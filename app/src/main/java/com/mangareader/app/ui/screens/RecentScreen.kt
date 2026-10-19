package com.mangareader.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.ui.components.ComicDialogHost
import com.mangareader.app.ui.components.ComicGridCard
import com.mangareader.app.ui.components.EmptyState
import com.mangareader.app.ui.components.MangaTopBar
import com.mangareader.app.ui.components.halftoneEdge
import com.mangareader.app.ui.components.rememberComicDialogs

@Composable
fun RecentScreen(
    onOpenSettings: () -> Unit,
    onOpenDetails: (Long) -> Unit,
    onRead: (Long) -> Unit,
    viewModel: RecentViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val dialogs = rememberComicDialogs()

    Column(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        MangaTopBar(title = stringResource(R.string.tab_recent), onOpenSettings = onOpenSettings)
        val current = ui
        when {
            current == null -> Unit
            current.reading.isEmpty() && current.added.isEmpty() ->
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Outlined.History,
                        stringResource(R.string.empty_recent_title),
                        stringResource(R.string.empty_recent_message),
                    )
                }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (current.reading.isNotEmpty()) {
                    item(key = "header-reading", span = { GridItemSpan(maxLineSpan) }) {
                        SectionTitle(stringResource(R.string.section_continue))
                    }
                    items(current.reading, key = { "r${it.id}" }) { comic ->
                        ComicGridCard(
                            comic,
                            dialogs,
                            onOpen = { onRead(comic.id) },
                            onOpenDetails = { onOpenDetails(comic.id) },
                        )
                    }
                }
                if (current.added.isNotEmpty()) {
                    item(key = "header-added", span = { GridItemSpan(maxLineSpan) }) {
                        SectionTitle(stringResource(R.string.section_added))
                    }
                    items(current.added, key = { "a${it.id}" }) { comic ->
                        ComicGridCard(
                            comic,
                            dialogs,
                            onOpen = { onRead(comic.id) },
                            onOpenDetails = { onOpenDetails(comic.id) },
                        )
                    }
                }
            }
        }
    }
    ComicDialogHost(dialogs)
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}
