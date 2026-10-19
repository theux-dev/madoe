package com.mangareader.app.data.source

import android.graphics.Bitmap
import java.io.Closeable

/** Acesso às páginas de um quadrinho, independente do formato do arquivo. */
interface PageSource : Closeable {
    val pageCount: Int

    /**
     * Decodifica a página [index] já reduzida (sem ficar muito maior que [maxWidth] x [maxHeight]),
     * para não carregar imagens enormes na memória.
     * @throws CorruptComicException se a página não puder ser lida.
     */
    fun loadPage(index: Int, maxWidth: Int, maxHeight: Int): Bitmap
}

class UnsupportedFormatException(message: String) : Exception(message)

class CorruptComicException(message: String, cause: Throwable? = null) : Exception(message, cause)
