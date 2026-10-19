package com.mangareader.app.ui.reader

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mangareader.app.R
import com.mangareader.app.data.db.BookmarkEntity
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.model.FitMode
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import com.mangareader.app.ui.components.formatDate
import com.mangareader.app.ui.settings.ChoiceSetting

/** Ajustes de leitura sem sair do leitor: valem para todos os quadrinhos e mudam a tela na hora. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderSettingsSheet(
    settings: AppSettings,
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.reader_settings), style = MaterialTheme.typography.titleLarge)
            ChoiceSetting(
                title = stringResource(R.string.setting_reading_mode),
                options = listOf(
                    ReadingMode.PAGED to stringResource(R.string.mode_paged),
                    ReadingMode.CONTINUOUS to stringResource(R.string.mode_continuous),
                ),
                selected = settings.readingMode,
                onSelect = viewModel::setReadingMode,
            )
            ChoiceSetting(
                title = stringResource(R.string.setting_direction),
                options = listOf(
                    ReadingDirection.LTR to stringResource(R.string.direction_ltr),
                    ReadingDirection.RTL to stringResource(R.string.direction_rtl),
                ),
                selected = settings.readingDirection,
                onSelect = viewModel::setReadingDirection,
            )
            ChoiceSetting(
                title = stringResource(R.string.setting_fit),
                options = listOf(
                    FitMode.WIDTH to stringResource(R.string.fit_width),
                    FitMode.HEIGHT to stringResource(R.string.fit_height),
                ),
                selected = settings.fitMode,
                onSelect = viewModel::setFitMode,
            )
            ChoiceSetting(
                title = stringResource(R.string.setting_navigation),
                options = listOf(
                    PageNavigation.GESTURES_AND_TAP to stringResource(R.string.nav_gestures_and_tap),
                    PageNavigation.GESTURES_ONLY to stringResource(R.string.nav_gestures_only),
                    PageNavigation.TAP_ONLY to stringResource(R.string.nav_tap_only),
                ),
                selected = settings.pageNavigation,
                onSelect = viewModel::setPageNavigation,
            )
            ChoiceSetting(
                title = stringResource(R.string.setting_orientation),
                options = listOf(
                    OrientationPref.AUTO to stringResource(R.string.orientation_auto),
                    OrientationPref.PORTRAIT to stringResource(R.string.orientation_portrait),
                    OrientationPref.LANDSCAPE to stringResource(R.string.orientation_landscape),
                ),
                selected = settings.orientation,
                onSelect = viewModel::setOrientation,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.night_mode),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = settings.readerNightMode, onCheckedChange = viewModel::setNightMode)
            }
        }
    }
}

@Composable
internal fun GoToPageDialog(pageCount: Int, onGo: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val target = text.toIntOrNull()
    val valid = target != null && target in 1..pageCount
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.go_to_page)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input -> text = input.filter(Char::isDigit).take(6) },
                label = { Text(stringResource(R.string.go_to_page_label, pageCount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { if (valid) onGo(target!! - 1) }),
            )
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onGo(target!! - 1) }) { Text(stringResource(R.string.go)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
internal fun BookmarksDialog(
    bookmarks: List<BookmarkEntity>,
    currentPage: Int,
    onGo: (Int) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val alreadyMarked = bookmarks.any { it.page == currentPage }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bookmarks)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (bookmarks.isEmpty()) {
                    Text(
                        stringResource(R.string.no_bookmarks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                bookmarks.forEach { bookmark ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onGo(bookmark.page) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.bookmark_page, bookmark.page + 1),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp),
                        )
                        IconButton(onClick = { onRemove(bookmark.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.bookmark_delete))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!alreadyMarked) {
                TextButton(onClick = onAdd) { Text(stringResource(R.string.bookmark_add)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
internal fun InfoDialog(comic: ComicEntity, currentPage: Int, pageCount: Int, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(comic.title, maxLines = 2) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoLine(stringResource(R.string.detail_format), ".${comic.format.label}")
                InfoLine(stringResource(R.string.detail_pages), pageCount.toString())
                InfoLine(stringResource(R.string.detail_size), Formatter.formatShortFileSize(context, comic.sizeBytes))
                InfoLine(stringResource(R.string.detail_added), formatDate(comic.addedAt))
                InfoLine(stringResource(R.string.detail_progress), "${(currentPage + 1) * 100 / pageCount.coerceAtLeast(1)}%")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
