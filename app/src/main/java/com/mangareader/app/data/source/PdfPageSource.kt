package com.mangareader.app.data.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.Closeable
import java.io.File
import java.io.IOException
import kotlin.math.min
import kotlin.math.sqrt

/** Páginas de um PDF renderizadas pelo `PdfRenderer` do Android (sem bibliotecas extras). */
class PdfPageSource private constructor(
    private val renderer: PdfRenderer,
    private val resources: List<Closeable>,
) : PageSource {

    override val pageCount: Int = renderer.pageCount

    // O PdfRenderer só permite uma página aberta por vez.
    @Synchronized
    override fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap {
        val page = try {
            renderer.openPage(index)
        } catch (e: Exception) {
            throw CorruptComicException("Não foi possível abrir a página ${index + 1}", e)
        }
        try {
            var scale = min(maxWidth.toFloat() / page.width, maxHeight.toFloat() / page.height)
            val pixels = page.width.toFloat() * page.height * scale * scale
            if (pixels > MAX_PIXELS) scale *= sqrt(MAX_PIXELS / pixels)
            val width = (page.width * scale).toInt().coerceAtLeast(1)
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        } finally {
            page.close()
        }
    }

    @Synchronized
    override fun close() {
        runCatching { renderer.close() }
        resources.forEach { runCatching { it.close() } }
    }

    companion object {
        private const val MAX_PIXELS = 16_000_000f

        fun open(context: Context, uri: Uri): PdfPageSource {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw CorruptComicException("Não foi possível abrir o PDF")
            try {
                return PdfPageSource(PdfRenderer(pfd), listOf(pfd))
            } catch (e: SecurityException) {
                pfd.close()
                throw CorruptComicException("PDF protegido por senha não é suportado", e)
            } catch (e: IOException) {
                pfd.close()
                return fromCacheCopy(context, uri)
            }
        }

        private fun fromCacheCopy(context: Context, uri: Uri): PdfPageSource {
            val file: File = try {
                context.copyToCache(uri, "pdf")
            } catch (e: IOException) {
                throw CorruptComicException("Não foi possível abrir o PDF", e)
            }
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            try {
                return PdfPageSource(PdfRenderer(pfd), listOf(pfd, Closeable { file.delete() }))
            } catch (e: Exception) {
                pfd.close()
                file.delete()
                throw CorruptComicException("PDF inválido, protegido ou corrompido", e)
            }
        }
    }
}
