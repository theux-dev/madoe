package com.mangareader.app.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mangareader.app.data.source.ArchivePageSource
import com.mangareader.app.data.source.CorruptComicException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchivePageSourceTest {
    private lateinit var context: Context
    private val files = mutableListOf<File>()

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After fun tearDown() = files.forEach { it.delete() }

    private fun tempFile(name: String) = File(context.cacheDir, "test-${System.nanoTime()}-$name").also { files += it }

    private fun png(width: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, 20, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    private fun zipOf(fileName: String, vararg entries: Pair<String, ByteArray>): Uri {
        val file = tempFile(fileName)
        ZipOutputStream(file.outputStream()).use { zip ->
            for ((name, data) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data)
                zip.closeEntry()
            }
        }
        return Uri.fromFile(file)
    }

    private fun tarOf(vararg entries: Pair<String, ByteArray>): Uri {
        val file = tempFile("test.cbt")
        TarArchiveOutputStream(file.outputStream()).use { tar ->
            for ((name, data) in entries) {
                tar.putArchiveEntry(TarArchiveEntry(name).apply { size = data.size.toLong() })
                tar.write(data)
                tar.closeArchiveEntry()
            }
        }
        return Uri.fromFile(file)
    }

    private fun sevenZOf(vararg entries: Pair<String, ByteArray>): Uri {
        val file = tempFile("test.cb7")
        SevenZOutputFile(file).use { out ->
            for ((name, data) in entries) {
                val source = tempFile("src-$name").also { it.writeBytes(data) }
                out.putArchiveEntry(out.createArchiveEntry(source, name))
                out.write(data)
                out.closeArchiveEntry()
            }
            out.finish()
        }
        return Uri.fromFile(file)
    }

    private fun assertWidthsInOrder(uri: Uri) {
        ArchivePageSource.open(context, uri).use { source ->
            assertEquals(3, source.pageCount)
            assertEquals(10, source.loadPage(0, 1000, 1000).width)
            assertEquals(20, source.loadPage(1, 1000, 1000).width)
            assertEquals(100, source.loadPage(2, 1000, 1000).width)
        }
    }

    private val pages = arrayOf(
        "10.png" to png(100),
        "2.png" to png(20),
        "1.png" to png(10),
    )

    @Test fun zipPagesFollowNaturalOrderAndSkipJunk() = assertWidthsInOrder(
        zipOf("test.cbz", *pages, "__MACOSX/1.png" to png(999), "notes.txt" to "x".toByteArray()),
    )

    @Test fun tarIsReadAsCbt() = assertWidthsInOrder(tarOf(*pages))

    @Test fun sevenZipIsReadAsCb7() = assertWidthsInOrder(sevenZOf(*pages))

    @Test fun zipWithCbrExtensionIsDetectedByContent() = assertWidthsInOrder(zipOf("fake.cbr", *pages))

    @Test(expected = CorruptComicException::class)
    fun archiveWithoutImagesIsRejected() {
        ArchivePageSource.open(context, zipOf("empty.cbz", "readme.txt" to "oi".toByteArray())).close()
    }

    @Test(expected = CorruptComicException::class)
    fun unknownContentIsRejected() {
        val file = tempFile("junk.cbz").also { it.writeText("isto não é um arquivo compactado") }
        ArchivePageSource.open(context, Uri.fromFile(file)).close()
    }
}
