package com.mangareader.app.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.readingProgress
import com.mangareader.app.ui.components.ComicActionRow
import com.mangareader.app.ui.components.ComicDialogHost
import com.mangareader.app.ui.components.ComicDialogs
import com.mangareader.app.ui.components.CoverImage
import com.mangareader.app.ui.components.FormatChip
import com.mangareader.app.ui.components.MangaBackTopBar
import com.mangareader.app.ui.components.ReadingProgressBar
import com.mangareader.app.ui.components.formatDate
import com.mangareader.app.ui.components.halftoneEdge
import com.mangareader.app.ui.components.rememberComicDialogs

@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    onRead: (Long) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val dialogs = rememberComicDialogs()

    // Se o título for removido (por aqui ou por outro lugar), não há mais o que mostrar.
    val loaded = ui as? DetailsUi.Loaded
    LaunchedEffect(loaded) {
        if (loaded != null && loaded.comic == null) onBack()
    }

    Column(
        Modifier
            .fillMaxSize()
            .halftoneEdge(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        MangaBackTopBar(title = stringResource(R.string.details_title), onBack = onBack)
        loaded?.comic?.let { comic -> DetailsContent(comic, dialogs, onRead) }
    }
    ComicDialogHost(dialogs)
}

@Composable
private fun DetailsContent(comic: ComicEntity, dialogs: ComicDialogs, onRead: (Long) -> Unit) {
    val context = LocalContext.current
    val progress = comic.readingProgress()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CoverImage(
            comic.coverPath,
            Modifier
                .fillMaxWidth(0.55f)
                .aspectRatio(2f / 3f),
        )
        Text(comic.title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        FormatChip(comic.format.label)

        Button(onClick = { onRead(comic.id) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (progress > 0f) R.string.continue_reading else R.string.read))
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRow(stringResource(R.string.detail_pages), comic.pageCount.toString())
                InfoRow(stringResource(R.string.detail_size), Formatter.formatShortFileSize(context, comic.sizeBytes))
                InfoRow(stringResource(R.string.detail_added), formatDate(comic.addedAt))
                InfoRow(
                    stringResource(R.string.detail_last_read),
                    comic.lastReadAt?.let(::formatDate) ?: stringResource(R.string.detail_never),
                )
                InfoRow(stringResource(R.string.detail_progress), "${(progress * 100).toInt()}%")
                ReadingProgressBar(progress)
            }
        }

        ComicActionRow(comic, dialogs, onOpenDetails = null, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
