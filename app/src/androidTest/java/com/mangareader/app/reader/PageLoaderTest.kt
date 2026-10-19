package com.mangareader.app.reader

import android.graphics.Bitmap
import com.mangareader.app.data.source.CorruptComicException
import com.mangareader.app.data.source.PageSource
import com.mangareader.app.ui.reader.PageLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PageLoaderTest {
    private class FakeSource(override val pageCount: Int, private val broken: Set<Int> = emptySet()) : PageSource {
        var loads = 0
        override fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap {
            loads++
            if (index in broken) throw CorruptComicException("página $index quebrada")
            return Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)
        }
        override fun close() = Unit
    }

    @Test fun secondRequestComesFromCache() = runTest {
        val source = FakeSource(3)
        val loader = PageLoader(source)
        val first = loader.load(1, 100, 100)
        val second = loader.load(1, 100, 100)
        assertNotNull(first)
        assertSame(first, second)
        assertEquals(1, source.loads)
    }

    @Test fun brokenPageReturnsNullInsteadOfCrashing() = runTest {
        val loader = PageLoader(FakeSource(3, broken = setOf(2)))
        assertNull(loader.load(2, 100, 100))
        assertNotNull(loader.load(0, 100, 100))
    }

    @Test fun outOfRangeIndexReturnsNull() = runTest {
        val loader = PageLoader(FakeSource(2))
        assertNull(loader.load(5, 100, 100))
        assertNull(loader.load(-1, 100, 100))
    }
}
