package com.mangareader.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.data.library.ImportResult
import com.mangareader.app.data.library.LibraryTaskState
import com.mangareader.app.data.model.SortOrder
import com.mangareader.app.ui.components.ComicDialogHost
import com.mangareader.app.ui.components.ComicListItem
import com.mangareader.app.ui.components.EmptyState
import com.mangareader.app.ui.components.MangaTopBar
import com.mangareader.app.ui.components.halftoneEdge
import com.mangareader.app.ui.components.rememberComicDialogs

@Composable
fun FilesScreen(
    onOpenSettings: () -> Unit,
    onOpenDetails: (Long) -> Unit,
    onRead: (Long) -> Unit,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val task by viewModel.taskState.collectAsStateWithLifecycle()
    val dialogs = rememberComicDialogs()
    var searching by rememberSaveable { mutableStateOf(false) }
    // O texto digitado fica aqui (e não só no ViewModel) para o cursor não "pular" durante a digitação.
    var searchText by rememberSaveable { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) viewModel.importFiles(uris)
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.addFolder(uri)
    }
    val pickFiles = { filePicker.launch(arrayOf("*/*")) }
    val pickFolder = { folderPicker.launch(null) }

    Box(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.fillMaxSize()) {
            val current = ui
            if (searching) {
                SearchField(
                    query = searchText,
                    onQueryChange = {
                        searchText = it
                        viewModel.setQuery(it)
                    },
                    onClose = {
                        searchText = ""
                        viewModel.setQuery("")
                        searching = false
                    },
                )
            } else {
                MangaTopBar(
                    title = stringResource(R.string.tab_files),
                    onOpenSettings = onOpenSettings,
                    actions = {
                        if (current != null && !current.libraryEmpty) {
                            IconButton(onClick = { searching = true }) {
                                Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search))
                            }
                            SortMenu(current.sort, viewModel::setSort)
                        }
                    },
                )
            }
            TaskBanner(task, onDismiss = viewModel::dismissResult)

            when {
                current == null -> Unit
                current.libraryEmpty -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.Description,
                        title = stringResource(R.string.empty_files_title),
                        message = stringResource(R.string.empty_files_message),
                    ) {
                        Button(onClick = pickFiles) { Text(stringResource(R.string.import_files)) }
                        OutlinedButton(onClick = pickFolder) { Text(stringResource(R.string.add_folder)) }
                    }
                }
                current.comics.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.no_results_title),
                        message = stringResource(R.string.no_results_message),
                    )
                }
                else -> LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(current.comics, key = { it.id }) { comic ->
                        ComicListItem(
                            comic,
                            dialogs,
                            onOpen = { onRead(comic.id) },
                            onOpenDetails = { onOpenDetails(comic.id) },
                        )
                    }
                }
            }
        }

        if (ui?.libraryEmpty == false) {
            var menuOpen by remember { mutableStateOf(false) }
            Box(Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                ExtendedFloatingActionButton(
                    onClick = { menuOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.add)) },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.import_files)) },
                        onClick = { menuOpen = false; pickFiles() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_folder)) },
                        onClick = { menuOpen = false; pickFolder() },
                    )
                }
            }
        }
    }

    ComicDialogHost(dialogs)
}

@Composable
private fun SortMenu(selected: SortOrder, onSelect: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Outlined.SwapVert, contentDescription = stringResource(R.string.sort))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf(
                SortOrder.NAME to R.string.sort_name,
                SortOrder.RECENTLY_ADDED to R.string.sort_added,
                SortOrder.LAST_READ to R.string.sort_last_read,
            ).forEach { (order, label) ->
                DropdownMenuItem(
                    text = { Text(stringResource(label)) },
                    onClick = { open = false; onSelect(order) },
                    trailingIcon = {
                        if (order == selected) Icon(Icons.Outlined.Check, contentDescription = null)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close_search))
            }
        },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(56.dp)
            .focusRequester(focus),
    )
}

@Composable
private fun TaskBanner(state: LibraryTaskState, onDismiss: () -> Unit) {
    when {
        state.running -> BannerSurface {
            Text(stringResource(R.string.importing), style = MaterialTheme.typography.titleMedium)
            state.message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        state.result != null -> ResultBanner(state.result, onDismiss)
    }
}

@Composable
private fun ResultBanner(result: ImportResult, onDismiss: () -> Unit) {
    BannerSurface {
        Text(stringResource(R.string.import_done), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.import_imported, result.imported))
        if (result.duplicates > 0) Text(stringResource(R.string.import_duplicates, result.duplicates))
        if (result.unsupported.isNotEmpty()) {
            Text(stringResource(R.string.import_unsupported, result.unsupported.size))
            DetailLines(result.unsupported)
        }
        if (result.failed.isNotEmpty()) {
            Text(stringResource(R.string.import_failed, result.failed.size))
            DetailLines(result.failed)
        }
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) }
    }
}

@Composable
private fun DetailLines(lines: List<String>) {
    lines.take(3).forEach {
        Text(
            "• $it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (lines.size > 3) {
        Text("…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BannerSurface(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
    }
}
