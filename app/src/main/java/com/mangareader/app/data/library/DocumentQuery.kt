package com.mangareader.app.data.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns

/** Nome e tamanho de um documento do SAF, ou null se o provedor não informar. */
internal fun Context.queryNameAndSize(uri: Uri): Pair<String, Long>? = runCatching {
    contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null,
    )?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val name = cursor.getString(0) ?: return@use null
        val size = if (cursor.isNull(1)) 0L else cursor.getLong(1)
        name to size
    }
}.getOrNull()

/** Texto amigável para uma pasta escolhida pelo usuário (ex.: "Download/HQs"). */
fun folderLabel(treeUri: String): String = runCatching {
    val id = DocumentsContract.getTreeDocumentId(Uri.parse(treeUri))
    id.substringAfter(':', "")
}.getOrDefault(treeUri)
