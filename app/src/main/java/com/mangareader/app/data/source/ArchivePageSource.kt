package com.mangareader.app.data.source

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.github.junrar.Archive
import java.io.Closeable
import java.io.File
import java.io.IOException
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarFile
import org.apache.commons.compress.archivers.zip.ZipFile

/**
 * Páginas de um quadrinho em arquivo compactado: CBZ/ZIP, CBR/RAR (incluindo RAR5), CB7/7z e CBT/TAR.
 * O tipo real é detectado pelo conteúdo, não pela extensão (é comum um .cbr ser na verdade um ZIP).
 */
class ArchivePageSource internal constructor(
    private val backend: ArchiveBackend,
) : PageSource {

    /** Índices (em [ArchiveBackend.entryNames]) das imagens, em ordem natural de nome. */
    private val pages: List<Int>

    init {
        val names = backend.entryNames.map { it.replace('\\', '/') }
        pages = names.withIndex()
            .filter { (_, name) ->
                val fileName = name.substringAfterLast('/')
                isImageName(fileName) && !fileName.startsWith(".") && !name.contains("__MACOSX")
            }
            .sortedWith { a, b -> naturalCompare(a.value, b.value) }
            .map { it.index }
        if (pages.isEmpty()) {
            backend.close()
            throw CorruptComicException("Nenhuma imagem encontrada no arquivo")
        }
    }

    override val pageCount: Int get() = pages.size

    override fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap {
        val bytes = try {
            backend.read(pages[index])
        } catch (e: Exception) {
            throw CorruptComicException("Não foi possível ler a página ${index + 1}", e)
        }
        return decodeSampled(bytes, maxWidth, maxHeight)
    }

    override fun close() = backend.close()

    companion object {
        fun open(context: Context, uri: Uri): ArchivePageSource {
            val backend = when (detectKind(context, uri)) {
                Kind.ZIP -> openBackend(
                    context, uri, "zip",
                    viaChannel = { s -> ZipBackend(ZipFile.builder().setSeekableByteChannel(s.channel).get(), listOf(s)) },
                    viaFile = { f, cleanup -> ZipBackend(ZipFile.builder().setFile(f).get(), listOf(cleanup)) },
                )
                Kind.SEVEN_Z -> openBackend(
                    context, uri, "7z",
                    viaChannel = { s -> SevenZBackend(SevenZFile.builder().setSeekableByteChannel(s.channel).get(), listOf(s)) },
                    viaFile = { f, cleanup -> SevenZBackend(SevenZFile.builder().setFile(f).get(), listOf(cleanup)) },
                )
                Kind.TAR -> openBackend(
                    context, uri, "tar",
                    viaChannel = { s -> TarBackend(TarFile(s.channel), listOf(s)) },
                    viaFile = { f, cleanup -> TarBackend(TarFile(f), listOf(cleanup)) },
                )
                Kind.RAR -> openBackend(
                    context, uri, "rar",
                    viaChannel = { s -> RarBackend(openRar(File(s.procPath)), listOf(s)) },
                    viaFile = { f, cleanup -> RarBackend(openRar(f), listOf(cleanup)) },
                )
                null -> throw CorruptComicException("Formato não reconhecido (não é ZIP, RAR, 7z nem TAR)")
            }
            return ArchivePageSource(backend)
        }

        private fun openRar(file: File): Archive {
            val archive = Archive(file)
            try {
                if (archive.isEncrypted) throw CorruptComicException("Arquivo protegido por senha não é suportado")
            } catch (e: Throwable) {
                runCatching { archive.close() }
                throw e
            }
            return archive
        }

        /** Tenta abrir direto pelo descritor do SAF; se o provedor não permitir, copia para o cache. */
        private fun <T : ArchiveBackend> openBackend(
            context: Context,
            uri: Uri,
            prefix: String,
            viaChannel: (Seekable) -> T,
            viaFile: (File, Closeable) -> T,
        ): T {
            val seekable = runCatching { context.openSeekable(uri) }.getOrNull()
            if (seekable != null) {
                try {
                    return viaChannel(seekable)
                } catch (e: CorruptComicException) {
                    seekable.close()
                    throw e
                } catch (e: Throwable) {
                    seekable.close()
                }
            }
            val file = try {
                context.copyToCache(uri, prefix)
            } catch (e: IOException) {
                throw CorruptComicException("Não foi possível abrir o arquivo", e)
            }
            try {
                return viaFile(file, Closeable { file.delete() })
            } catch (e: CorruptComicException) {
                file.delete()
                throw e
            } catch (e: Exception) {
                file.delete()
                throw CorruptComicException("Arquivo inválido ou corrompido", e)
            }
        }

        private enum class Kind { ZIP, RAR, SEVEN_Z, TAR }

        private fun detectKind(context: Context, uri: Uri): Kind? {
            val head = ByteArray(HEAD_SIZE)
            val read = try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    var total = 0
                    while (total < head.size) {
                        val n = input.read(head, total, head.size - total)
                        if (n < 0) break
                        total += n
                    }
                    total
                } ?: throw CorruptComicException("Não foi possível abrir o arquivo")
            } catch (e: IOException) {
                throw CorruptComicException("Não foi possível ler o arquivo", e)
            }
            fun at(i: Int) = head[i].toInt() and 0xFF
            return when {
                read >= 4 && at(0) == 0x50 && at(1) == 0x4B && at(2) in 3..7 -> Kind.ZIP
                read >= 6 && at(0) == 0x52 && at(1) == 0x61 && at(2) == 0x72 && at(3) == 0x21 &&
                    at(4) == 0x1A && at(5) == 0x07 -> Kind.RAR
                read >= 6 && at(0) == 0x37 && at(1) == 0x7A && at(2) == 0xBC && at(3) == 0xAF &&
                    at(4) == 0x27 && at(5) == 0x1C -> Kind.SEVEN_Z
                read >= 262 && String(head, 257, 5, Charsets.ISO_8859_1) == "ustar" -> Kind.TAR
                else -> null
            }
        }

        private const val HEAD_SIZE = 265
    }
}
