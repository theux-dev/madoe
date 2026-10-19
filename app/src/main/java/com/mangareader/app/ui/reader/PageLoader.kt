package com.mangareader.app.ui.reader

import android.graphics.Bitmap
import android.util.LruCache
import com.mangareader.app.data.source.PageSource
import java.io.Closeable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Carrega páginas sob demanda e mantém só algumas em memória (cache LRU por bytes).
 * Nunca carrega o arquivo inteiro: cada página é decodificada reduzida, só quando alguém pede.
 */
class PageLoader(private val source: PageSource) : Closeable {

    val pageCount: Int get() = source.pageCount

    private val cache = object : LruCache<Int, Bitmap>(cacheBytes()) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
    }

    /** @return a página, ou `null` se não foi possível lê-la (arquivo corrompido ou falta de memória). */
    suspend fun load(index: Int, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (index !in 0 until pageCount) return null
        cache.get(index)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                source.loadPage(index, maxWidth, maxHeight).also { cache.put(index, it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                cache.evictAll()
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun close() {
        cache.evictAll()
        runCatching { source.close() }
    }

    private companion object {
        fun cacheBytes(): Int {
            val quarter = Runtime.getRuntime().maxMemory() / 4
            return quarter.coerceIn(MIN_CACHE_BYTES, MAX_CACHE_BYTES).toInt()
        }

        const val MIN_CACHE_BYTES = 32L * 1024 * 1024
        const val MAX_CACHE_BYTES = 192L * 1024 * 1024
    }
}
