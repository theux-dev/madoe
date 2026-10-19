package com.mangareader.app.data.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Percorre uma pasta escolhida pelo usuário (SAF) procurando arquivos pelo nome. */
class FolderScanner(private val context: Context) {

    data class Found(val uri: Uri, val name: String, val sizeBytes: Long)

    suspend fun findFiles(treeUri: Uri, wanted: (String) -> Boolean): List<Found> {
        val results = mutableListOf<Found>()
        walk(treeUri, DocumentsContract.getTreeDocumentId(treeUri), 0, results, wanted)
        return results
    }

    private suspend fun walk(
        treeUri: Uri,
        documentId: String,
        depth: Int,
        results: MutableList<Found>,
        wanted: (String) -> Boolean,
    ) {
        if (depth > MAX_DEPTH) return
        currentCoroutineContext().ensureActive()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val columns = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
        )
        val subfolders = mutableListOf<String>()
        val cursor = runCatching { context.contentResolver.query(childrenUri, columns, null, null, null) }
            .getOrNull()
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(0) ?: continue
                val name = it.getString(1) ?: continue
                val mime = it.getString(2)
                if (mime == Document.MIME_TYPE_DIR) {
                    if (!name.startsWith(".")) subfolders += id
                } else if (wanted(name)) {
                    val size = if (it.isNull(3)) 0L else it.getLong(3)
                    results += Found(DocumentsContract.buildDocumentUriUsingTree(treeUri, id), name, size)
                }
            }
        }
        subfolders.forEach { walk(treeUri, it, depth + 1, results, wanted) }
    }

    private companion object {
        const val MAX_DEPTH = 10
    }
}
