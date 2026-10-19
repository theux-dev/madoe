package com.mangareader.app.data.source

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.Closeable
import java.io.IOException
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile

/** Páginas de um CBZ/ZIP: as imagens do arquivo, em ordem natural de nome. */
class ZipPageSource private constructor(
    private val zip: ZipFile,
    private val entries: List<ZipArchiveEntry>,
    private val resources: List<Closeable>,
) : PageSource {

    override val pageCount: Int get() = entries.size

    override fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap {
        val bytes = try {
            synchronized(zip) { zip.getInputStream(entries[index]).use { it.readBytes() } }
        } catch (e: IOException) {
            throw CorruptComicException("Não foi possível ler a página ${index + 1}", e)
        }
        return decodeSampled(bytes, maxWidth, maxHeight)
    }

    override fun close() {
        runCatching { zip.close() }
        resources.forEach { runCatching { it.close() } }
    }

    companion object {
        fun open(context: Context, uri: Uri): ZipPageSource {
            runCatching { fromChannel(context, uri) }.getOrNull()?.let { return it }
            return fromCacheCopy(context, uri)
        }

        private fun fromChannel(context: Context, uri: Uri): ZipPageSource {
            val seekable = context.openSeekable(uri)
            try {
                val zip = ZipFile.builder().setSeekableByteChannel(seekable.channel).get()
                return create(zip, listOf(seekable))
            } catch (e: Throwable) {
                seekable.close()
                throw e
            }
        }

        private fun fromCacheCopy(context: Context, uri: Uri): ZipPageSource {
            val file = try {
                context.copyToCache(uri, "zip")
            } catch (e: IOException) {
                throw CorruptComicException("Não foi possível abrir o arquivo", e)
            }
            try {
                val zip = ZipFile.builder().setFile(file).get()
                return create(zip, listOf(Closeable { file.delete() }))
            } catch (e: CorruptComicException) {
                file.delete()
                throw e
            } catch (e: Exception) {
                file.delete()
                throw CorruptComicException("Arquivo ZIP/CBZ inválido ou corrompido", e)
            }
        }

        private fun create(zip: ZipFile, resources: List<Closeable>): ZipPageSource {
            val images = zip.entries.asSequence()
                .filter { entry ->
                    val fileName = entry.name.substringAfterLast('/')
                    !entry.isDirectory &&
                        isImageName(fileName) &&
                        !fileName.startsWith(".") &&
                        !entry.name.contains("__MACOSX")
                }
                .sortedWith { a, b -> naturalCompare(a.name, b.name) }
                .toList()
            if (images.isEmpty()) {
                zip.close()
                throw CorruptComicException("Nenhuma imagem encontrada no arquivo")
            }
            return ZipPageSource(zip, images, resources)
        }
    }
}
