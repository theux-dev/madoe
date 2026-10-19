package com.mangareader.app.ui.settings

import android.content.Context
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.data.library.folderLabel
import com.mangareader.app.data.model.FitMode
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import com.mangareader.app.data.model.ThemeMode
import com.mangareader.app.ui.components.MangaBackTopBar
import com.mangareader.app.ui.components.halftoneEdge

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val s by viewModel.settings.collectAsStateWithLifecycle()
    val task by viewModel.libraryState.collectAsStateWithLifecycle()
    val storage by viewModel.storage.collectAsStateWithLifecycle()
    val freed by viewModel.freedBytes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.addFolder(uri)
    }

    Column(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        MangaBackTopBar(title = stringResource(R.string.settings), onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard(stringResource(R.string.settings_section_appearance)) {
                ChoiceSetting(
                    title = stringResource(R.string.setting_theme),
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                        ThemeMode.DARK to stringResource(R.string.theme_dark),
                    ),
                    selected = s.themeMode,
                    onSelect = viewModel::setThemeMode,
                )
            }

            SettingsCard(stringResource(R.string.settings_section_library)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.setting_auto_scan),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                    )
                    Switch(checked = s.autoScanOnStart, onCheckedChange = viewModel::setAutoScanOnStart)
                }
                Text(stringResource(R.string.setting_folders), style = MaterialTheme.typography.titleMedium)
                if (s.libraryFolders.isEmpty()) {
                    Text(
                        stringResource(R.string.no_folders),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                s.libraryFolders.sorted().forEach { folder ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            folderLabel(folder).ifEmpty { stringResource(R.string.internal_storage) },
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.removeFolder(folder) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.remove_folder))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { folderPicker.launch(null) }) { Text(stringResource(R.string.add_folder)) }
                    OutlinedButton(
                        onClick = viewModel::scanNow,
                        enabled = !task.running && s.libraryFolders.isNotEmpty(),
                    ) { Text(stringResource(R.string.scan_now)) }
                }
                if (task.running) {
                    Text(
                        stringResource(R.string.importing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SettingsCard(stringResource(R.string.settings_section_storage)) {
                StorageRow(stringResource(R.string.storage_covers), Formatter.formatShortFileSize(context, storage.coversBytes))
                StorageRow(stringResource(R.string.storage_cache), Formatter.formatShortFileSize(context, storage.cacheBytes))
                OutlinedButton(
                    onClick = viewModel::clearCache,
                    enabled = !task.running && storage.cacheBytes > 0,
                ) { Text(stringResource(R.string.clear_cache)) }
                freed?.let {
                    Text(
                        stringResource(R.string.cache_cleared, Formatter.formatShortFileSize(context, it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    stringResource(R.string.storage_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsCard(stringResource(R.string.settings_section_reading)) {
                ChoiceSetting(
                    title = stringResource(R.string.setting_direction),
                    options = listOf(
                        ReadingDirection.LTR to stringResource(R.string.direction_ltr),
                        ReadingDirection.RTL to stringResource(R.string.direction_rtl),
                    ),
                    selected = s.readingDirection,
                    onSelect = viewModel::setReadingDirection,
                )
                ChoiceSetting(
                    title = stringResource(R.string.setting_reading_mode),
                    options = listOf(
                        ReadingMode.PAGED to stringResource(R.string.mode_paged),
                        ReadingMode.CONTINUOUS to stringResource(R.string.mode_continuous),
                    ),
                    selected = s.readingMode,
                    onSelect = viewModel::setReadingMode,
                )
                ChoiceSetting(
                    title = stringResource(R.string.setting_fit),
                    options = listOf(
                        FitMode.WIDTH to stringResource(R.string.fit_width),
                        FitMode.HEIGHT to stringResource(R.string.fit_height),
                    ),
                    selected = s.fitMode,
                    onSelect = viewModel::setFitMode,
                )
                ChoiceSetting(
                    title = stringResource(R.string.setting_navigation),
                    options = listOf(
                        PageNavigation.GESTURES_AND_TAP to stringResource(R.string.nav_gestures_and_tap),
                        PageNavigation.GESTURES_ONLY to stringResource(R.string.nav_gestures_only),
                        PageNavigation.TAP_ONLY to stringResource(R.string.nav_tap_only),
                    ),
                    selected = s.pageNavigation,
                    onSelect = viewModel::setPageNavigation,
                )
                ChoiceSetting(
                    title = stringResource(R.string.setting_orientation),
                    options = listOf(
                        OrientationPref.AUTO to stringResource(R.string.orientation_auto),
                        OrientationPref.PORTRAIT to stringResource(R.string.orientation_portrait),
                        OrientationPref.LANDSCAPE to stringResource(R.string.orientation_landscape),
                    ),
                    selected = s.orientation,
                    onSelect = viewModel::setOrientation,
                )
            }
            SettingsCard(stringResource(R.string.settings_section_about)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.app_slogan),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
                Text(
                    stringResource(R.string.about_version, appVersion(context)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(R.string.about_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceSetting(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun appVersion(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
}.getOrNull() ?: "?"
