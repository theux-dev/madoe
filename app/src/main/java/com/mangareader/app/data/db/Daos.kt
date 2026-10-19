package com.mangareader.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    @Query("SELECT * FROM comics")
    fun observeAll(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE lastReadAt IS NOT NULL ORDER BY lastReadAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavorites(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE id = :id")
    fun observeById(id: Long): Flow<ComicEntity?>

    @Query("SELECT COUNT(*) FROM comics WHERE uri = :uri")
    suspend fun countByUri(uri: String): Int

    @Query("SELECT * FROM comics WHERE id = :id")
    suspend fun getById(id: Long): ComicEntity?

    /** Retorna -1 quando o mesmo URI já existe na biblioteca. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(comic: ComicEntity): Long

    @Query("UPDATE comics SET currentPage = :page, lastReadAt = :now WHERE id = :id")
    suspend fun updateProgress(id: Long, page: Int, now: Long)

    @Query("UPDATE comics SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE comics SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    /** Remove apenas o registro da biblioteca; o arquivo original não é tocado. */
    @Query("DELETE FROM comics WHERE id = :id")
    suspend fun deleteFromLibrary(id: Long)
}

@Dao
interface CollectionDao {
    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.createdAt AS createdAt, COUNT(x.comicId) AS comicCount
        FROM collections c
        LEFT JOIN collection_comics x ON x.collectionId = c.id
        GROUP BY c.id
        ORDER BY c.createdAt DESC
        """
    )
    fun observeSummaries(): Flow<List<CollectionSummary>>

    @Query(
        """
        SELECT cm.* FROM comics cm
        INNER JOIN collection_comics x ON x.comicId = cm.id
        WHERE x.collectionId = :collectionId
        ORDER BY x.addedAt DESC
        """
    )
    fun observeComics(collectionId: Long): Flow<List<ComicEntity>>

    @Query(
        """
        SELECT x.collectionId AS collectionId, cm.coverPath AS coverPath, cm.pageCount AS pageCount,
               cm.currentPage AS currentPage, cm.lastReadAt AS lastReadAt
        FROM collection_comics x
        INNER JOIN comics cm ON cm.id = x.comicId
        ORDER BY x.addedAt DESC
        """
    )
    fun observeMembers(): Flow<List<CollectionMember>>

    @Query("SELECT collectionId FROM collection_comics WHERE comicId = :comicId")
    fun observeCollectionIdsFor(comicId: Long): Flow<List<Long>>

    @Insert
    suspend fun insert(collection: CollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addComic(ref: CollectionComicCrossRef)

    @Query("DELETE FROM collection_comics WHERE collectionId = :collectionId AND comicId = :comicId")
    suspend fun removeComic(collectionId: Long, comicId: Long)

    @Query("UPDATE collections SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    /** Apaga a coleção; os quadrinhos continuam na biblioteca. */
    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE comicId = :comicId ORDER BY page")
    fun observe(comicId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun delete(id: Long)
}
