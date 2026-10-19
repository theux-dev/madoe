package com.mangareader.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.vector.ImageVector
import com.mangareader.app.R

object Routes {
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val DETAILS = "details/{comicId}"
    const val COLLECTION = "collection/{collectionId}"
    const val READER = "reader/{comicId}"

    fun details(comicId: Long) = "details/$comicId"
    fun collection(collectionId: Long) = "collection/$collectionId"
    fun reader(comicId: Long) = "reader/$comicId"
}

/** Abas da barra inferior, na mesma ordem das referências. */
enum class TopLevelTab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    RECENT("recent", R.string.tab_recent, Icons.Outlined.History),
    FILES("files", R.string.tab_files, Icons.Outlined.Description),
    FEATURED("featured", R.string.tab_featured, Icons.Outlined.FavoriteBorder),
    COLLECTIONS("collections", R.string.tab_collections, Icons.Outlined.CollectionsBookmark),
}
