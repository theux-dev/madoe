package com.mangareader.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mangareader.app.data.model.ComicFormat

/**
 * Um título da biblioteca. [uri] aponta para o arquivo original (SAF); o app nunca o modifica nem o apaga.
 */
@Entity(tableName = "comics", indices = [Index(value = ["uri"], unique = true)])
data class ComicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
    val format: ComicFormat,
    val sizeBytes: Long,
    val pageCount: Int = 0,
    val addedAt: Long,
    val lastReadAt: Long? = null,
    val currentPage: Int = 0,
    val isFavorite: Boolean = false,
    val coverPath: String? = null,
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "collection_comics",
    primaryKeys = ["collectionId", "comicId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ComicEntity::class,
            parentColumns = ["id"],
            childColumns = ["comicId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("comicId")],
)
data class CollectionComicCrossRef(
    val collectionId: Long,
    val comicId: Long,
    val addedAt: Long,
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = ComicEntity::class,
            parentColumns = ["id"],
            childColumns = ["comicId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["comicId", "page"], unique = true)],
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val comicId: Long,
    val page: Int,
    val createdAt: Long,
)

/** Resultado de consulta: coleção com contagem de títulos. */
data class CollectionSummary(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val comicCount: Int,
)

/** Um título dentro de uma coleção, só com o necessário para montar a capa e o progresso. */
data class CollectionMember(
    val collectionId: Long,
    val coverPath: String?,
    val pageCount: Int,
    val currentPage: Int,
    val lastReadAt: Long?,
)
