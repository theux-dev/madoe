package com.mangareader.app.data.repo

import com.mangareader.app.data.db.CollectionComicCrossRef
import com.mangareader.app.data.db.CollectionDao
import com.mangareader.app.data.db.CollectionEntity
import com.mangareader.app.data.db.CollectionMember
import com.mangareader.app.data.db.CollectionSummary
import com.mangareader.app.data.db.ComicEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CollectionRepository @Inject constructor(private val dao: CollectionDao) {
    fun observeSummaries(): Flow<List<CollectionSummary>> = dao.observeSummaries()
    fun observeMembers(): Flow<List<CollectionMember>> = dao.observeMembers()
    fun observeCollectionIdsFor(comicId: Long): Flow<List<Long>> = dao.observeCollectionIdsFor(comicId)
    fun observeComics(collectionId: Long): Flow<List<ComicEntity>> = dao.observeComics(collectionId)

    suspend fun create(name: String): Long =
        dao.insert(CollectionEntity(name = name.trim(), createdAt = System.currentTimeMillis()))

    suspend fun addComic(collectionId: Long, comicId: Long) =
        dao.addComic(CollectionComicCrossRef(collectionId, comicId, System.currentTimeMillis()))

    suspend fun removeComic(collectionId: Long, comicId: Long) = dao.removeComic(collectionId, comicId)
    suspend fun rename(id: Long, name: String) = dao.rename(id, name.trim())
    suspend fun delete(id: Long) = dao.delete(id)
}
