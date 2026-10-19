package com.mangareader.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.data.db.ComicEntity

/** Estado dos diálogos abertos a partir dos cards de título (um por tela). */
@Stable
class ComicDialogs(val actions: ComicActionsViewModel) {
    var rename by mutableStateOf<ComicEntity?>(null)
    var remove by mutableStateOf<ComicEntity?>(null)
    var collections by mutableStateOf<ComicEntity?>(null)
    var newCollectionFor by mutableStateOf<ComicEntity?>(null)
}

@Composable
fun rememberComicDialogs(actions: ComicActionsViewModel = hiltViewModel()): ComicDialogs =
    remember(actions) { ComicDialogs(actions) }

/** Linha de ações do card: favorito, coleção e menu ⋮. */
@Composable
fun ComicActionRow(
    comic: ComicEntity,
    dialogs: ComicDialogs,
    onOpenDetails: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onRemoveFromCollection: (() -> Unit)? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { dialogs.actions.toggleFavorite(comic) }) {
            Icon(
                if (comic.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (comic.isFavorite) R.string.favorite_remove else R.string.favorite_add,
                ),
            )
        }
        IconButton(onClick = { dialogs.collections = comic }) {
            Icon(
                Icons.Outlined.CollectionsBookmark,
                contentDescription = stringResource(R.string.pick_collection_title),
            )
        }
        Spacer(Modifier.weight(1f))
        ComicOverflowMenu(comic, dialogs, onOpenDetails, onRemoveFromCollection)
    }
}

@Composable
private fun ComicOverflowMenu(
    comic: ComicEntity,
    dialogs: ComicDialogs,
    onOpenDetails: (() -> Unit)?,
    onRemoveFromCollection: (() -> Unit)?,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (onOpenDetails != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_details)) },
                    onClick = { open = false; onOpenDetails() },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_rename)) },
                onClick = { open = false; dialogs.rename = comic },
            )
            if (onRemoveFromCollection != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_remove_from_collection)) },
                    onClick = { open = false; onRemoveFromCollection() },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_remove_library)) },
                onClick = { open = false; dialogs.remove = comic },
            )
        }
    }
}

/** Diálogo simples de texto (renomear, criar coleção). */
@Composable
fun NameDialog(
    title: String,
    label: String,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onConfirm(text.trim()) }),
            )
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.trim()) }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** Renderiza os diálogos pedidos pelos cards. Coloque uma vez por tela. */
@Composable
fun ComicDialogHost(dialogs: ComicDialogs) {
    dialogs.rename?.let { comic ->
        NameDialog(
            title = stringResource(R.string.rename_title),
            label = stringResource(R.string.rename_label),
            initial = comic.title,
            confirmLabel = stringResource(R.string.save),
            onConfirm = {
                dialogs.actions.rename(comic.id, it)
                dialogs.rename = null
            },
            onDismiss = { dialogs.rename = null },
        )
    }

    dialogs.remove?.let { comic ->
        AlertDialog(
            onDismissRequest = { dialogs.remove = null },
            title = { Text(stringResource(R.string.remove_title)) },
            text = { Text(stringResource(R.string.remove_message, comic.title)) },
            confirmButton = {
                TextButton(onClick = {
                    dialogs.actions.removeFromLibrary(comic.id)
                    dialogs.remove = null
                }) { Text(stringResource(R.string.remove_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogs.remove = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    dialogs.collections?.let { comic ->
        val collections by dialogs.actions.collections.collectAsStateWithLifecycle()
        val memberOf by remember(comic.id) { dialogs.actions.collectionIdsFor(comic.id) }
            .collectAsStateWithLifecycle(initialValue = emptySet())

        AlertDialog(
            onDismissRequest = { dialogs.collections = null },
            title = { Text(stringResource(R.string.pick_collection_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (collections.isEmpty()) {
                        Text(
                            stringResource(R.string.no_collections_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    collections.forEach { collection ->
                        val checked = collection.id in memberOf
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { dialogs.actions.setInCollection(collection.id, comic.id, !checked) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(collection.name, Modifier.padding(start = 12.dp))
                        }
                    }
                    TextButton(
                        onClick = { dialogs.newCollectionFor = comic },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(
                            stringResource(R.string.new_collection),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogs.collections = null }) { Text(stringResource(R.string.done)) }
            },
        )
    }

    dialogs.newCollectionFor?.let { comic ->
        NameDialog(
            title = stringResource(R.string.new_collection),
            label = stringResource(R.string.collection_name),
            initial = "",
            confirmLabel = stringResource(R.string.create),
            onConfirm = {
                dialogs.actions.createCollectionAndAdd(it, comic.id)
                dialogs.newCollectionFor = null
            },
            onDismiss = { dialogs.newCollectionFor = null },
        )
    }
}
