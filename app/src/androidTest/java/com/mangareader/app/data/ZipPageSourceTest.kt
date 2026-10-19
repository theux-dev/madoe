package com.mangareader.app.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mangareader.app.data.source.CorruptComicException
import com.mangareader.app.data.source.ZipPageSource
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZipPageSourceTest {
    private lateinit var context: Context
    private val files = mutableListOf<File>()

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After fun tearDown() = files.forEach { it.delete() }

    private fun png(width: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, 20, Bitmap.Config.ARGB_8888)
        return java.io.ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    private fun zipOf(vararg entries: Pair<String, ByteArray>): Uri {
        val file = File(context.cacheDir, "test-${System.nanoTime()}.cbz").also { files += it }
        ZipOutputStream(file.outputStream()).use { zip ->
            for ((name, data) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data)
                zip.closeEntry()
            }
        }
        return Uri.fromFile(file)
    }

    @Test fun pagesFollowNaturalOrderAndSkipJunk() {
        val uri = zipOf(
            "10.png" to png(100),
            "2.png" to png(20),
            "1.png" to png(10),
            "__MACOSX/1.png" to png(999),
            "notes.txt" to "x".toByteArray(),
        )
        ZipPageSource.open(context, uri).use { source ->
            assertEquals(3, source.pageCount)
            assertEquals(10, source.loadPage(0, 1000, 1000).width)
            assertEquals(20, source.loadPage(1, 1000, 1000).width)
            assertEquals(100, source.loadPage(2, 1000, 1000).width)
        }
    }

    @Test(expected = CorruptComicException::class)
    fun archiveWithoutImagesIsRejected() {
        ZipPageSource.open(context, zipOf("readme.txt" to "oi".toByteArray())).close()
    }
}
