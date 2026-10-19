package com.mangareader.app.data.library

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.mangareader.app.R
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.ComicFormat
import com.mangareader.app.data.repo.ComicRepository
import com.mangareader.app.data.source.CorruptComicException
import com.mangareader.app.data.source.IMAGE_URI_SEPARATOR
import com.mangareader.app.data.source.PageSource
import com.mangareader.app.data.source.PageSourceFactory
import com.mangareader.app.data.source.naturalCompare
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Valida um arquivo (abre e conta as páginas), gera a capa e registra o título no banco.
 * O arquivo original nunca é copiado nem alterado: guardamos só o URI.
 */
@Singleton
class ComicImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val factory: PageSourceFactory,
    private val repository: ComicRepository,
) {
    private val coversDir: File by lazy { File(context.filesDir, "covers").also { it.mkdirs() } }

    suspend fun importDocument(uri: Uri, displayName: String, sizeBytes: Long): ImportResult {
        val format = ComicFormat.fromFileName(displayName)
        if (format == null || format == ComicFormat.IMAGE || !factory.isSupported(format)) {
            return ImportResult(unsupported = listOf(displayName))
        }
        val key = uri.toString()
        if (repository.exists(key)) return ImportResult(duplicates = 1)
        return addTitle(key, displayName.substringBeforeLast('.'), format, sizeBytes)
    }

    /** Imagens soltas escolhidas juntas viram um único título, na ordem natural dos nomes. */
    suspend fun importImages(images: List<Triple<Uri, String, Long>>): ImportResult {
        if (images.isEmpty()) return ImportResult()
        val sorted = images.sortedWith { a, b -> naturalCompare(a.second, b.second) }
        val key = sorted.joinToString(IMAGE_URI_SEPARATOR) { it.first.toString() }
        if (repository.exists(key)) return ImportResult(duplicates = 1)
        val title = if (sorted.size == 1) {
            sorted[0].second.substringBeforeLast('.')
        } else {
            context.getString(R.string.images_title, sorted.size)
        }
        return addTitle(key, title, ComicFormat.IMAGE, sorted.sumOf { it.third })
    }

    private suspend fun addTitle(
        key: String,
        title: String,
        format: ComicFormat,
        sizeBytes: Long,
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            factory.open(format, key).use { source ->
                if (source.pageCount <= 0) throw CorruptComicException("Nenhuma página encontrada")
                val cover = saveCover(source, key)
                val id = repository.add(
                    ComicEntity(
                        uri = key,
                        title = title,
                        format = format,
                        sizeBytes = sizeBytes,
                        pageCount = source.pageCount,
                        addedAt = System.currentTimeMillis(),
                        coverPath = cover,
                    ),
                )
                if (id == null) ImportResult(duplicates = 1) else ImportResult(imported = 1)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OutOfMemoryError) {
            ImportResult(failed = listOf("$title: memória insuficiente"))
        } catch (e: Exception) {
            ImportResult(failed = listOf("$title: ${e.message ?: e.javaClass.simpleName}"))
        }
    }

    private fun saveCover(source: PageSource, key: String): String? = runCatching {
        val bitmap = source.loadPage(0, COVER_WIDTH, COVER_HEIGHT)
        val file = File(coversDir, sha1(key) + ".jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        bitmap.recycle()
        file.absolutePath
    }.getOrNull()

    private fun sha1(text: String): String =
        MessageDigest.getInstance("SHA-1").digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val COVER_WIDTH = 480
        const val COVER_HEIGHT = 720
    }
}
