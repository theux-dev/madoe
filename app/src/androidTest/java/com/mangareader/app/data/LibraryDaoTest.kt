package com.mangareader.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mangareader.app.data.db.AppDatabase
import com.mangareader.app.data.db.CollectionComicCrossRef
import com.mangareader.app.data.db.CollectionEntity
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.ComicFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun tearDown() = db.close()

    private fun comic(uri: String, title: String = "T") = ComicEntity(
        uri = uri, title = title, format = ComicFormat.CBZ, sizeBytes = 10, pageCount = 20, addedAt = 1L,
    )

    @Test fun duplicateUriIsIgnored() = runTest {
        val first = db.comicDao().insert(comic("content://a"))
        val second = db.comicDao().insert(comic("content://a"))
        assertNotEquals(-1L, first)
        assertEquals(-1L, second)
        assertEquals(1, db.comicDao().observeAll().first().size)
    }

    @Test fun progressMovesComicToRecent() = runTest {
        val id = db.comicDao().insert(comic("content://a"))
        assertTrue(db.comicDao().observeRecent(10).first().isEmpty())
        db.comicDao().updateProgress(id, page = 7, now = 99L)
        val recent = db.comicDao().observeRecent(10).first()
        assertEquals(7, recent.single().currentPage)
        assertEquals(99L, recent.single().lastReadAt)
    }

    @Test fun favoritesAreFiltered() = runTest {
        val a = db.comicDao().insert(comic("content://a"))
        db.comicDao().insert(comic("content://b"))
        db.comicDao().setFavorite(a, true)
        assertEquals(listOf(a), db.comicDao().observeFavorites().first().map { it.id })
    }

    @Test fun deletingCollectionKeepsComics() = runTest {
        val comicId = db.comicDao().insert(comic("content://a"))
        val colId = db.collectionDao().insert(CollectionEntity(name = "Heróis", createdAt = 1L))
        db.collectionDao().addComic(CollectionComicCrossRef(colId, comicId, 1L))
        assertEquals(1, db.collectionDao().observeSummaries().first().single().comicCount)
        db.collectionDao().delete(colId)
        assertTrue(db.collectionDao().observeSummaries().first().isEmpty())
        assertEquals(1, db.comicDao().observeAll().first().size)
    }

    @Test fun removingComicLeavesCollectionEmpty() = runTest {
        val comicId = db.comicDao().insert(comic("content://a"))
        val colId = db.collectionDao().insert(CollectionEntity(name = "X", createdAt = 1L))
        db.collectionDao().addComic(CollectionComicCrossRef(colId, comicId, 1L))
        db.comicDao().deleteFromLibrary(comicId)
        assertEquals(0, db.collectionDao().observeSummaries().first().single().comicCount)
    }

    @Test fun membersExposeCoverAndProgress() = runTest {
        val id = db.comicDao().insert(comic("content://a").copy(coverPath = "/covers/a.jpg"))
        db.comicDao().updateProgress(id, page = 4, now = 5L)
        val colId = db.collectionDao().insert(CollectionEntity(name = "X", createdAt = 1L))
        db.collectionDao().addComic(CollectionComicCrossRef(colId, id, 1L))
        val member = db.collectionDao().observeMembers().first().single()
        assertEquals(colId, member.collectionId)
        assertEquals("/covers/a.jpg", member.coverPath)
        assertEquals(4, member.currentPage)
        assertEquals(listOf(colId), db.collectionDao().observeCollectionIdsFor(id).first())
    }

    /** Reproduz o fluxo "criar coleção pelo card": cria e já adiciona o título, em sequência. */
    @Test fun createCollectionAndAddInSequenceLinksTheComic() = runTest {
        val comicId = db.comicDao().insert(comic("content://a"))
        val colId = db.collectionDao().insert(CollectionEntity(name = "Nova", createdAt = 1L))
        db.collectionDao().addComic(CollectionComicCrossRef(colId, comicId, 2L))
        val summary = db.collectionDao().observeSummaries().first().single()
        assertEquals("Nova", summary.name)
        assertEquals(1, summary.comicCount)
        assertEquals(listOf(comicId), db.collectionDao().observeComics(colId).first().map { it.id })
    }
}
