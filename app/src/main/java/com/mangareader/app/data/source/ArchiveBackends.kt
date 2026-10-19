package com.mangareader.app.data.source

import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import java.io.ByteArrayOutputStream
import java.io.Closeable
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile

/** Lista as entradas (arquivos, sem pastas) de um contêiner e lê uma delas inteira. */
internal interface ArchiveBackend : Closeable {
    val entryNames: List<String>
    fun read(index: Int): ByteArray
}

private fun List<Closeable>.closeAll() = forEach { runCatching { it.close() } }

internal class ZipBackend(
    private val zip: ZipFile,
    private val resources: List<Closeable>,
) : ArchiveBackend {
    private val entries: List<ZipArchiveEntry> = zip.entries.asSequence().filter { !it.isDirectory }.toList()
    override val entryNames: List<String> = entries.map { it.name }

    override fun read(index: Int): ByteArray =
        synchronized(zip) { zip.getInputStream(entries[index]).use { it.readBytes() } }

    override fun close() {
        runCatching { zip.close() }
        resources.closeAll()
    }
}

internal class TarBackend(
    private val tar: TarFile,
    private val resources: List<Closeable>,
) : ArchiveBackend {
    private val entries: List<TarArchiveEntry> = tar.entries.filter { it.isFile }
    override val entryNames: List<String> = entries.map { it.name }

    override fun read(index: Int): ByteArray =
        synchronized(tar) { tar.getInputStream(entries[index]).use { it.readBytes() } }

    override fun close() {
        runCatching { tar.close() }
        resources.closeAll()
    }
}

internal class SevenZBackend(
    private val sevenZ: SevenZFile,
    private val resources: List<Closeable>,
) : ArchiveBackend {
    private val entries: List<SevenZArchiveEntry> =
        sevenZ.entries.filter { !it.isDirectory && it.hasStream() }
    override val entryNames: List<String> = entries.map { it.name }

    override fun read(index: Int): ByteArray =
        synchronized(sevenZ) { sevenZ.getInputStream(entries[index]).use { it.readBytes() } }

    override fun close() {
        runCatching { sevenZ.close() }
        resources.closeAll()
    }
}

internal class RarBackend(
    private val rar: Archive,
    private val resources: List<Closeable>,
) : ArchiveBackend {
    private val headers: List<FileHeader> = rar.fileHeaders.filter { !it.isDirectory }
    override val entryNames: List<String> = headers.map { it.fileName }

    override fun read(index: Int): ByteArray = synchronized(rar) {
        val out = ByteArrayOutputStream()
        rar.extractFile(headers[index], out)
        out.toByteArray()
    }

    override fun close() {
        runCatching { rar.close() }
        resources.closeAll()
    }
}
