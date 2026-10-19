package com.mangareader.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.mangareader.app.R
import com.mangareader.app.ui.components.EmptyState
import com.mangareader.app.ui.components.MangaTopBar
import com.mangareader.app.ui.components.halftoneEdge

/**
 * Abas ainda sem conteúdo próprio: mostram o estado vazio real.
 * Recentes depende do leitor (fase 4); Favoritos e Coleções ganham telas na fase 3.
 * A aba Arquivos está em [FilesScreen].
 */
@Composable
private fun TabScreen(
    title: String,
    icon: ImageVector,
    emptyTitle: String,
    emptyMessage: String,
    onOpenSettings: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        MangaTopBar(title = title, onOpenSettings = onOpenSettings)
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(icon, emptyTitle, emptyMessage)
        }
    }
}

@Composable
fun RecentScreen(onOpenSettings: () -> Unit) = TabScreen(
    title = stringResource(R.string.tab_recent),
    icon = Icons.Outlined.History,
    emptyTitle = stringResource(R.string.empty_recent_title),
    emptyMessage = stringResource(R.string.empty_recent_message),
    onOpenSettings = onOpenSettings,
)

@Composable
fun FeaturedScreen(onOpenSettings: () -> Unit) = TabScreen(
    title = stringResource(R.string.tab_featured),
    icon = Icons.Outlined.FavoriteBorder,
    emptyTitle = stringResource(R.string.empty_featured_title),
    emptyMessage = stringResource(R.string.empty_featured_message),
    onOpenSettings = onOpenSettings,
)

@Composable
fun CollectionsScreen(onOpenSettings: () -> Unit) = TabScreen(
    title = stringResource(R.string.tab_collections),
    icon = Icons.Outlined.CollectionsBookmark,
    emptyTitle = stringResource(R.string.empty_collections_title),
    emptyMessage = stringResource(R.string.empty_collections_message),
    onOpenSettings = onOpenSettings,
)
