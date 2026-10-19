package com.mangareader.app.data

import com.mangareader.app.data.storage.clearContents
import com.mangareader.app.data.storage.totalSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class StorageUtilsTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun missingFolderHasZeroSize() {
        assertEquals(0L, tmp.root.resolve("nao-existe").totalSize())
    }

    @Test fun sizeIsRecursive() {
        tmp.newFile("a.bin").writeBytes(ByteArray(100))
        val sub = tmp.newFolder("sub")
        sub.resolve("b.bin").writeBytes(ByteArray(50))
        assertEquals(150L, tmp.root.totalSize())
    }

    @Test fun clearContentsFreesBytesButKeepsTheFolder() {
        tmp.newFile("a.bin").writeBytes(ByteArray(100))
        tmp.newFolder("sub").resolve("b.bin").writeBytes(ByteArray(50))
        val freed = tmp.root.clearContents()
        assertEquals(150L, freed)
        assertTrue(tmp.root.exists())
        assertEquals(0L, tmp.root.totalSize())
    }
}
