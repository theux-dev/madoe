package com.mangareader.app.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mangareader.app.R
import com.mangareader.app.data.model.AppIcon

/** Escolha do ícone do app: uma prévia de cada ícone e uma confirmação antes de trocar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppIconSetting(
    selected: AppIcon,
    failed: Boolean,
    onSelect: (AppIcon) -> Unit,
) {
    var pending by remember { mutableStateOf<AppIcon?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.setting_app_icon), style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon.entries.forEach { icon ->
                IconOption(
                    icon = icon,
                    selected = icon == selected,
                    onClick = { if (icon != selected) pending = icon },
                )
            }
        }
        Text(
            stringResource(R.string.app_icon_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (failed) {
            Text(
                stringResource(R.string.app_icon_change_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    pending?.let { icon ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(stringResource(R.string.app_icon_change_title, stringResource(iconLabel(icon)))) },
            text = { Text(stringResource(R.string.app_icon_change_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onSelect(icon)
                    pending = null
                }) { Text(stringResource(R.string.app_icon_change_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun IconOption(icon: AppIcon, selected: Boolean, onClick: () -> Unit) {
    val art = iconArt(icon)
    val shape = RoundedCornerShape(18.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = shape,
                )
                .clip(shape)
                .background(colorResource(art.background)),
        ) {
            // O desenho do ícone adaptativo tem 108dp e só o centro de 72dp aparece no launcher:
            // por isso a imagem é maior que a caixa e o excesso é cortado pelo clip.
            Image(
                painter = painterResource(art.foreground),
                contentDescription = null,
                modifier = Modifier.requiredSize(96.dp),
            )
        }
        Text(
            stringResource(iconLabel(icon)),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private class IconArt(@DrawableRes val foreground: Int, val background: Int)

private fun iconArt(icon: AppIcon): IconArt = when (icon) {
    AppIcon.MADOE -> IconArt(R.drawable.ic_launcher_foreground, R.color.ic_launcher_background)
    AppIcon.PAPER -> IconArt(R.drawable.ic_launcher_foreground_paper, R.color.ic_launcher_background_paper)
    AppIcon.OCEAN -> IconArt(R.drawable.ic_launcher_foreground_ocean, R.color.ic_launcher_background_ocean)
    AppIcon.SAKURA -> IconArt(R.drawable.ic_launcher_foreground_sakura, R.color.ic_launcher_background_sakura)
}

@StringRes
private fun iconLabel(icon: AppIcon): Int = when (icon) {
    AppIcon.MADOE -> R.string.app_icon_madoe
    AppIcon.PAPER -> R.string.app_icon_paper
    AppIcon.OCEAN -> R.string.app_icon_ocean
    AppIcon.SAKURA -> R.string.app_icon_sakura
}
