package com.mangareader.app.ui.reader

internal const val MAX_ZOOM = 5f
internal const val DOUBLE_TAP_ZOOM = 2.5f

/**
 * Nova translação para que o ponto [focus] (distância até o ponto de referência da ampliação)
 * continue sob os dedos quando o zoom muda pelo fator [ratio] (zoom novo / zoom antigo).
 */
internal fun focusedTranslation(translation: Float, focus: Float, ratio: Float): Float =
    focus * (1f - ratio) + translation * ratio

internal fun clampTranslation(value: Float, maxAbs: Float): Float = value.coerceIn(-maxAbs, maxAbs)

/** Resolução pedida ao decodificar (múltiplo do tamanho da tela): mais nítida quando há zoom. */
internal fun zoomQuality(zoom: Float): Float = if (zoom > 1.4f) 3f else 1.5f
