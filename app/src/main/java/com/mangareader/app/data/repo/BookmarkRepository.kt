package com.mangareader.app.data.repo

import com.mangareader.app.data.db.BookmarkDao
import com.mangareader.app.data.db.BookmarkEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BookmarkRepository @Inject constructor(private val dao: BookmarkDao) {
    fun observe(comicId: Long): Flow<List<BookmarkEntity>> = dao.observe(comicId)

    suspend fun add(comicId: Long, page: Int) {
        dao.insert(BookmarkEntity(comicId = comicId, page = page, createdAt = System.currentTimeMillis()))
    }

    suspend fun remove(id: Long) = dao.delete(id)
}
