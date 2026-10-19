package com.mangareader.app.data.source

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel

/** Acesso aleatório a um documento do SAF. Fechar libera o descritor. */
internal class Seekable(pfd: ParcelFileDescriptor) : Closeable {
    private val stream = ParcelFileDescriptor.AutoCloseInputStream(pfd)
    val channel: FileChannel = stream.channel

    /** Caminho que bibliotecas baseadas em `File` conseguem abrir enquanto este descritor estiver aberto. */
    val procPath: String = "/proc/self/fd/${pfd.fd}"

    override fun close() = stream.close()
}

internal fun Context.openSeekable(uri: Uri): Seekable {
    val pfd = contentResolver.openFileDescriptor(uri, "r")
        ?: throw IOException("Não foi possível abrir o arquivo")
    return Seekable(pfd)
}

/** Plano B para provedores que não permitem acesso aleatório: copia o arquivo para o cache do app. */
internal fun Context.copyToCache(uri: Uri, prefix: String): File {
    val dir = File(cacheDir, "imports").apply { mkdirs() }
    val out = File(dir, "$prefix-${System.nanoTime()}.tmp")
    try {
        val input = contentResolver.openInputStream(uri) ?: throw IOException("Não foi possível abrir o arquivo")
        input.use { source -> out.outputStream().use { source.copyTo(it) } }
    } catch (e: Throwable) {
        out.delete()
        throw e
    }
    return out
}
