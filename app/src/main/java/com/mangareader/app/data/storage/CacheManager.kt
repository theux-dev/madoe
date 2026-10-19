package com.mangareader.app.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StorageInfo(
    /** Capas geradas na importação (ficam na pasta interna do app). */
    val coversBytes: Long = 0,
    /** Cópias provisórias e imagens em cache; pode ser apagado sem perder nada. */
    val cacheBytes: Long = 0,
)

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun info(): StorageInfo = withContext(Dispatchers.IO) {
        StorageInfo(
            coversBytes = File(context.filesDir, "covers").totalSize(),
            cacheBytes = context.cacheDir.totalSize(),
        )
    }

    /** Esvazia o cache do app. Os quadrinhos, as capas e o progresso de leitura não são tocados. */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) { context.cacheDir.clearContents() }
}
