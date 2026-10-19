package com.mangareader.app.data.source

import android.content.Context
import android.net.Uri
import com.mangareader.app.data.model.ComicFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Separador usado para guardar vários URIs de imagens soltas no campo `uri` de um título. */
const val IMAGE_URI_SEPARATOR = "\n"

@Singleton
class PageSourceFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Todos os formatos reconhecidos pela biblioteca têm leitor implementado. */
    fun isSupported(format: ComicFormat): Boolean = true

    fun open(format: ComicFormat, uri: String): PageSource = when (format) {
        ComicFormat.CBZ, ComicFormat.ZIP, ComicFormat.CBR, ComicFormat.CB7, ComicFormat.CBT ->
            ArchivePageSource.open(context, Uri.parse(uri))
        ComicFormat.PDF -> PdfPageSource.open(context, Uri.parse(uri))
        ComicFormat.IMAGE -> ImageListPageSource(context, uri.split(IMAGE_URI_SEPARATOR).map(Uri::parse))
    }
}
