package com.mangareader.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** Decoração de meio-tom (bolinhas) na borda esquerda, como nas referências. */
@Composable
fun Modifier.halftoneEdge(color: Color = MaterialTheme.colorScheme.surfaceVariant): Modifier =
    drawBehind {
        val step = 14.dp.toPx()
        val maxRadius = 5.dp.toPx()
        val columns = 6
        val rows = (size.height / step).toInt() + 1
        for (row in 0..rows) {
            val vertical = 0.3f + 0.7f * ((sin(row * 0.33f) + 1f) / 2f)
            for (col in 0 until columns) {
                val radius = maxRadius * (1f - col / columns.toFloat()) * vertical
                if (radius < 0.8f) continue
                val x = col * step + if (row % 2 == 0) 0f else step / 2f
                drawCircle(color, radius, Offset(x, row * step))
            }
        }
    }
