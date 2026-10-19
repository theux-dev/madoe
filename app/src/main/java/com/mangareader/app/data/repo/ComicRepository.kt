package com.mangareader.app.data.repo

import com.mangareader.app.data.db.ComicDao
import com.mangareader.app.data.db.ComicEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ComicRepository @Inject constructor(private val dao: ComicDao) {
    fun observeAll(): Flow<List<ComicEntity>> = dao.observeAll()
    fun observeRecent(limit: Int = 50): Flow<List<ComicEntity>> = dao.observeRecent(limit)
    fun observeFavorites(): Flow<List<ComicEntity>> = dao.observeFavorites()
    fun observeById(id: Long): Flow<ComicEntity?> = dao.observeById(id)

    suspend fun exists(uri: String): Boolean = dao.countByUri(uri) > 0

    suspend fun getById(id: Long): ComicEntity? = dao.getById(id)

    /** @return id do novo título, ou null se o arquivo já estava na biblioteca. */
    suspend fun add(comic: ComicEntity): Long? = dao.insert(comic).takeIf { it != -1L }

    suspend fun saveProgress(id: Long, page: Int) =
        dao.updateProgress(id, page, System.currentTimeMillis())

    suspend fun setFavorite(id: Long, favorite: Boolean) = dao.setFavorite(id, favorite)
    suspend fun rename(id: Long, title: String) = dao.rename(id, title.trim())
    /** Remove só o registro e a capa gerada; o arquivo original nunca é tocado. */
    suspend fun removeFromLibrary(id: Long) {
        val cover = dao.getById(id)?.coverPath
        dao.deleteFromLibrary(id)
        cover?.let { runCatching { File(it).delete() } }
    }
}
