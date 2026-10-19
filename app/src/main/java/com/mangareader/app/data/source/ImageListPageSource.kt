package com.mangareader.app.data.source

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.IOException

/** Um título formado por imagens soltas (JPG, PNG, WebP), uma página por imagem. */
class ImageListPageSource(
    private val context: Context,
    private val uris: List<Uri>,
) : PageSource {

    override val pageCount: Int get() = uris.size

    override fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap {
        val bytes = try {
            context.contentResolver.openInputStream(uris[index])?.use { it.readBytes() }
        } catch (e: IOException) {
            throw CorruptComicException("Não foi possível ler a imagem ${index + 1}", e)
        } catch (e: SecurityException) {
            throw CorruptComicException("Sem permissão para ler a imagem ${index + 1}", e)
        } ?: throw CorruptComicException("Imagem ${index + 1} inacessível")
        return decodeSampled(bytes, maxWidth, maxHeight)
    }

    override fun close() = Unit
}
