package com.mangareader.app.data

import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.ComicFormat
import com.mangareader.app.data.model.readingProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingProgressTest {
    private fun comic(page: Int, total: Int, lastRead: Long?) = ComicEntity(
        uri = "content://x", title = "t", format = ComicFormat.CBZ, sizeBytes = 1,
        pageCount = total, addedAt = 0, lastReadAt = lastRead, currentPage = page,
    )

    @Test fun neverOpenedIsZero() = assertEquals(0f, comic(0, 10, null).readingProgress(), 0f)
    @Test fun halfwayIsHalf() = assertEquals(0.5f, comic(4, 10, 1L).readingProgress(), 0.0001f)
    @Test fun lastPageIsComplete() = assertEquals(1f, comic(9, 10, 1L).readingProgress(), 0f)
    @Test fun unknownPageCountIsZero() = assertEquals(0f, comic(3, 0, 1L).readingProgress(), 0f)

    @Test fun formatFromFileName() {
        assertEquals(ComicFormat.CBR, ComicFormat.fromFileName("Avatar.CBR"))
        assertEquals(ComicFormat.IMAGE, ComicFormat.fromFileName("page.webp"))
        assertEquals(null, ComicFormat.fromFileName("notes.txt"))
    }
}
