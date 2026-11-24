package com.mangareader.app.data

import com.mangareader.app.data.source.exportFileName
import com.mangareader.app.data.source.mimeForExtension
import com.mangareader.app.data.source.sniffImageExtension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageExportTest {
    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    @Test fun fileNameRemovesAccentsAndSymbols() {
        assertEquals("Nho-Quim_p007.jpg", exportFileName("Nhô-Quim", 7, "JPG"))
        assertEquals("The_Flame_001_p012.png", exportFileName("The Flame #001", 12, "png"))
    }

    @Test fun fileNameHasAFallbackForEmptyTitles() {
        assertEquals("madoe_p001.jpg", exportFileName("???", 1, "jpg"))
        assertEquals("madoe_p100.webp", exportFileName("   ", 100, "webp"))
    }

    @Test fun fileNameIsLimitedInLength() {
        val name = exportFileName("a".repeat(200), 1, "jpg")
        assertEquals(60 + "_p001.jpg".length, name.length)
    }

    @Test fun sniffRecognizesCommonImageTypes() {
        assertEquals("jpg", sniffImageExtension(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0)))
        assertEquals("png", sniffImageExtension(bytes(0x89, 'P'.code, 'N'.code, 'G'.code, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertEquals("gif", sniffImageExtension("GIF89a".toByteArray()))
        assertEquals("bmp", sniffImageExtension("BM\u0000\u0000".toByteArray()))
        assertEquals("webp", sniffImageExtension("RIFF1234WEBPVP8 ".toByteArray()))
    }

    @Test fun sniffRejectsUnknownAndShortData() {
        assertNull(sniffImageExtension(ByteArray(0)))
        assertNull(sniffImageExtension("texto qualquer".toByteArray()))
    }

    @Test fun mimeTypesFollowTheExtension() {
        assertEquals("image/jpeg", mimeForExtension("jpeg"))
        assertEquals("image/png", mimeForExtension("PNG"))
        assertNull(mimeForExtension("txt"))
    }
}
