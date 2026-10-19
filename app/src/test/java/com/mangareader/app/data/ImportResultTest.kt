package com.mangareader.app.data

import com.mangareader.app.data.library.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportResultTest {
    @Test fun resultsAreSummed() {
        val total = ImportResult(imported = 1, unsupported = listOf("a.txt")) +
            ImportResult(imported = 2, duplicates = 1, failed = listOf("x: erro"))
        assertEquals(3, total.imported)
        assertEquals(1, total.duplicates)
        assertEquals(listOf("a.txt"), total.unsupported)
        assertEquals(listOf("x: erro"), total.failed)
    }

    @Test fun emptyResultIsNeutral() {
        val r = ImportResult(imported = 4)
        assertEquals(r, r + ImportResult())
    }
}
