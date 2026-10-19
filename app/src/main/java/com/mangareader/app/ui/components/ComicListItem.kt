package com.mangareader.app.ui.components

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mangareader.app.R
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.readingProgress

/** Card em lista (aba Arquivos e detalhe de coleção): capa, nome, formato, tamanho, progresso e ações. */
@Composable
fun ComicListItem(
    comic: ComicEntity,
    dialogs: ComicDialogs,
    onOpen: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
    onRemoveFromCollection: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val shape = MaterialTheme.shapes.large
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CoverImage(comic.coverPath, Modifier.size(width = 84.dp, height = 126.dp))
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = 126.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        comic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FormatChip(comic.format.label)
                    Text(
                        "${Formatter.formatShortFileSize(context, comic.sizeBytes)} · " +
                            stringResource(R.string.pages_count, comic.pageCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ReadingProgressBar(comic.readingProgress(), Modifier.padding(top = 8.dp))
                ComicActionRow(comic, dialogs, onOpenDetails = onOpenDetails, onRemoveFromCollection = onRemoveFromCollection)
            }
        }
    }
}
