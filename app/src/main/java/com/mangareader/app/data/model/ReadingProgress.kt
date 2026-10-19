package com.mangareader.app.data.model

import com.mangareader.app.data.db.ComicEntity

/** Fração lida (0..1). Zero enquanto o título nunca foi aberto. */
fun progressOf(pageCount: Int, currentPage: Int, lastReadAt: Long?): Float = when {
    lastReadAt == null || pageCount <= 0 -> 0f
    else -> ((currentPage + 1).toFloat() / pageCount).coerceIn(0f, 1f)
}

fun ComicEntity.readingProgress(): Float = progressOf(pageCount, currentPage, lastReadAt)
