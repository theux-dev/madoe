package com.mangareader.app.data.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import com.mangareader.app.data.model.ComicFormat
import com.mangareader.app.data.prefs.SettingsRepository
import com.mangareader.app.data.repo.ComicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Orquestra importações e varreduras de pastas. As tarefas rodam uma de cada vez,
 * fora do ciclo de vida das telas (continuam se o usuário trocar de aba).
 */
@Singleton
class LibraryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importer: ComicImporter,
    private val repository: ComicRepository,
    private val settings: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val scanner = FolderScanner(context)

    private val _state = MutableStateFlow(LibraryTaskState())
    val state: StateFlow<LibraryTaskState> = _state.asStateFlow()

    private var lastAutoScan: Long? = null

    fun dismissResult() = _state.update { it.copy(result = null) }

    /** Importa arquivos escolhidos no seletor de documentos. */
    fun importFiles(uris: List<Uri>) = launchTask(showResult = true) { progress ->
        val entries = uris.mapNotNull { uri ->
            keepAccess(uri)
            context.queryNameAndSize(uri)?.let { (name, size) -> Triple(uri, name, size) }
        }
        val (images, documents) = entries.partition { ComicFormat.fromFileName(it.second) == ComicFormat.IMAGE }
        var total = ImportResult()
        for ((uri, name, size) in documents) {
            currentCoroutineContext().ensureActive()
            progress(name)
            total += importer.importDocument(uri, name, size)
        }
        total + importer.importImages(images)
    }

    /** Guarda a pasta como monitorada e escaneia na hora. */
    fun addFolder(treeUri: Uri) = launchTask(showResult = true) { progress ->
        keepAccess(treeUri)
        settings.addLibraryFolder(treeUri.toString())
        scanTree(treeUri, progress)
    }

    fun removeFolder(treeUri: String) {
        scope.launch {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(treeUri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            settings.removeLibraryFolder(treeUri)
        }
    }

    /**
     * Procura arquivos novos em todas as pastas monitoradas.
     * Automático (ao abrir o app): silencioso, só avisa se algo novo entrou.
     */
    fun rescanAll(userInitiated: Boolean) {
        if (!userInitiated) {
            val now = SystemClock.elapsedRealtime()
            val last = lastAutoScan
            if (last != null && now - last < AUTO_SCAN_MIN_INTERVAL_MS) return
            lastAutoScan = now
        }
        launchTask(showResult = userInitiated) { progress ->
            var total = ImportResult()
            for (folder in settings.settings.first().libraryFolders) {
                total += scanTree(Uri.parse(folder), progress)
            }
            total
        }
    }

    private suspend fun scanTree(treeUri: Uri, progress: (String) -> Unit): ImportResult {
        val files = scanner.findFiles(treeUri) { name ->
            ComicFormat.fromFileName(name).let { it != null && it != ComicFormat.IMAGE }
        }
        var total = ImportResult()
        for (file in files) {
            currentCoroutineContext().ensureActive()
            if (repository.exists(file.uri.toString())) continue
            progress(file.name)
            total += importer.importDocument(file.uri, file.name, file.sizeBytes)
        }
        return total
    }

    private fun keepAccess(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun launchTask(showResult: Boolean, block: suspend ((String) -> Unit) -> ImportResult) {
        scope.launch {
            mutex.withLock {
                _state.value = LibraryTaskState(running = true)
                val result = try {
                    block { name -> _state.update { it.copy(message = name) } }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    ImportResult(failed = listOf("Erro inesperado: ${e.message ?: e.javaClass.simpleName}"))
                }
                _state.value = LibraryTaskState(
                    running = false,
                    result = result.takeIf { showResult || it.imported > 0 },
                )
            }
        }
    }

    private companion object {
        const val AUTO_SCAN_MIN_INTERVAL_MS = 60_000L
    }
}
