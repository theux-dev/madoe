package com.mangareader.app.data.storage

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.mangareader.app.data.source.OriginalPage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface SaveResult {
    data class Saved(val fileName: String) : SaveResult
    data object Unsupported : SaveResult
    data class Failed(val reason: String?) : SaveResult
}

/**
 * Salva a imagem de uma página na galeria (pasta Pictures/Madoe) pelo MediaStore.
 * No Android 10 ou mais novo isso não exige nenhuma permissão. Antes do Android 10 o recurso não é oferecido.
 */
@Singleton
class PageSaver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val isSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    suspend fun save(page: OriginalPage, fileName: String): SaveResult = withContext(Dispatchers.IO) {
        if (!isSupported) SaveResult.Unsupported else saveWithMediaStore(page, fileName)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWithMediaStore(page: OriginalPage, fileName: String): SaveResult {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, page.mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Madoe")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return SaveResult.Failed("Não foi possível criar o arquivo")
        return try {
            val output = resolver.openOutputStream(uri) ?: throw IOException("Não foi possível gravar o arquivo")
            output.use { it.write(page.bytes) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            SaveResult.Saved(fileName)
        } catch (e: Exception) {
            runCatching { resolver.delete(uri, null, null) }
            SaveResult.Failed(e.message)
        }
    }
}
