package com.mangareader.app.data.storage

import java.io.File

/** Tamanho total, em bytes, de um arquivo ou de uma pasta (recursivo). */
fun File.totalSize(): Long = when {
    !exists() -> 0L
    isFile -> length()
    else -> listFiles()?.sumOf { it.totalSize() } ?: 0L
}

/** Apaga o conteúdo da pasta, mantendo a pasta. Retorna quantos bytes foram liberados. */
fun File.clearContents(): Long {
    var freed = 0L
    listFiles()?.forEach { child ->
        val size = child.totalSize()
        if (child.deleteRecursively()) freed += size
    }
    return freed
}
