package com.mangareader.app.data

import com.mangareader.app.data.source.naturalCompare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalOrderTest {
    @Test fun numbersAreComparedByValue() {
        assertTrue(naturalCompare("page2.jpg", "page10.jpg") < 0)
        assertTrue(naturalCompare("page10.jpg", "page2.jpg") > 0)
    }

    @Test fun leadingZerosAreIgnored() {
        assertTrue(naturalCompare("001.png", "2.png") < 0)
    }

    @Test fun caseIsIgnored() {
        assertEquals(0, naturalCompare("Capa.JPG", "capa.jpg"))
    }

    @Test fun sortsLikeAFileManager() {
        val sorted = listOf("10.jpg", "2.jpg", "1.jpg", "cover.jpg").sortedWith { a, b -> naturalCompare(a, b) }
        assertEquals(listOf("1.jpg", "2.jpg", "10.jpg", "cover.jpg"), sorted)
    }
}
