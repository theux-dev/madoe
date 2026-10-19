package com.mangareader.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.ui.components.ComicDialogHost
import com.mangareader.app.ui.components.ComicListItem
import com.mangareader.app.ui.components.EmptyState
import com.mangareader.app.ui.components.MangaBackTopBar
import com.mangareader.app.ui.components.NameDialog
import com.mangareader.app.ui.components.halftoneEdge
import com.mangareader.app.ui.components.rememberComicDialogs

@Composable
fun CollectionDetailScreen(
    onBack: () -> Unit,
    onOpenDetails: (Long) -> Unit,
    onRead: (Long) -> Unit,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val comics by viewModel.comics.collectAsStateWithLifecycle()
    val dialogs = rememberComicDialogs()
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        MangaBackTopBar(
            title = collection?.name.orEmpty(),
            onBack = onBack,
            actions = {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rename_collection)) },
                            onClick = { menuOpen = false; renaming = true },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_collection)) },
                            onClick = { menuOpen = false; deleting = true },
                        )
                    }
                }
            },
        )

        val list = comics
        when {
            list == null -> Unit
            list.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Outlined.CollectionsBookmark,
                    stringResource(R.string.empty_collection_title),
                    stringResource(R.string.empty_collection_message),
                )
            }
            else -> LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.id }) { comic ->
                    ComicListItem(
                        comic = comic,
                        dialogs = dialogs,
                        onOpen = { onRead(comic.id) },
                        onOpenDetails = { onOpenDetails(comic.id) },
                        onRemoveFromCollection = { viewModel.removeComic(comic.id) },
                    )
                }
            }
        }
    }

    ComicDialogHost(dialogs)

    if (renaming) {
        NameDialog(
            title = stringResource(R.string.rename_collection),
            label = stringResource(R.string.collection_name),
            initial = collection?.name.orEmpty(),
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                viewModel.rename(it)
                renaming = false
            },
            onDismiss = { renaming = false },
        )
    }

    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text(stringResource(R.string.delete_collection_title)) },
            text = { Text(stringResource(R.string.delete_collection_message, collection?.name.orEmpty())) },
            confirmButton = {
                TextButton(onClick = {
                    deleting = false
                    viewModel.delete()
                    onBack()
                }) { Text(stringResource(R.string.remove_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
