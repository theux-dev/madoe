package com.mangareader.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.ui.components.CoverImage
import com.mangareader.app.ui.components.EmptyState
import com.mangareader.app.ui.components.MangaTopBar
import com.mangareader.app.ui.components.NameDialog
import com.mangareader.app.ui.components.ReadingProgressBar
import com.mangareader.app.ui.components.formatDate
import com.mangareader.app.ui.components.halftoneEdge

@Composable
fun CollectionsScreen(
    onOpenSettings: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    var creating by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.fillMaxSize()) {
            MangaTopBar(title = stringResource(R.string.tab_collections), onOpenSettings = onOpenSettings)
            val list = collections
            when {
                list == null -> Unit
                list.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Outlined.CollectionsBookmark,
                        stringResource(R.string.empty_collections_title),
                        stringResource(R.string.empty_collections_message),
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(list, key = { it.id }) { item ->
                        CollectionCard(item, onClick = { onOpenCollection(item.id) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.new_collection)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (creating) {
        NameDialog(
            title = stringResource(R.string.new_collection),
            label = stringResource(R.string.collection_name),
            initial = "",
            confirmLabel = stringResource(R.string.create),
            onConfirm = {
                viewModel.create(it)
                creating = false
            },
            onDismiss = { creating = false },
        )
    }
}

@Composable
private fun CollectionCard(item: CollectionUi, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CoverMosaic(item.covers, Modifier.fillMaxWidth().aspectRatio(1f))
            Text(
                stringResource(R.string.collection_title_count, item.name, item.count),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatDate(item.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.reading_progress_percent, (item.progress * 100).toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ReadingProgressBar(item.progress)
        }
    }
}

/** 0 capas: ícone; 1 capa: preenche tudo; 2 a 4 capas: mosaico 2×2. */
@Composable
private fun CoverMosaic(covers: List<String>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    when (covers.size) {
        0 -> CoverImage(null, modifier, shape)
        1 -> CoverImage(covers[0], modifier, shape)
        else -> Column(modifier.clip(shape), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            covers.chunked(2).forEach { rowCovers ->
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    rowCovers.forEach { path ->
                        CoverImage(path, Modifier.weight(1f).fillMaxSize(), RoundedCornerShape(0.dp))
                    }
                    if (rowCovers.size == 1) {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        )
                    }
                }
            }
        }
    }
}
