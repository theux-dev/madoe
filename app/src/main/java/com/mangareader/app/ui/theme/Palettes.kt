package com.mangareader.app.ui.theme

import com.mangareader.app.data.model.AppPalette
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Cores-base de uma paleta, em ARGB. Ficam fora dos tipos do Compose para que o contraste de cada
 * combinação possa ser conferido em testes de unidade.
 */
data class PaletteSeed(
    val primary: Long,
    val onPrimary: Long,
    val background: Long,
    val onBackground: Long,
    /** Fundo dos cartões (e do indicador da barra inferior). */
    val card: Long,
    /** Texto secundário sobre fundo e cartões. */
    val onCard: Long,
    val outline: Long,
)

object Palettes {
    private val madoeLight = PaletteSeed(0xFF3E7C78, 0xFFFFFFFF, 0xFFFDF8F0, 0xFF1C1B17, 0xFFBFC1A0, 0xFF4A4A40, 0xFF4A4A40)
    private val madoeDark = PaletteSeed(0xFF7CC4BE, 0xFF00201E, 0xFF1B1D18, 0xFFEDE9DC, 0xFF3A3D2F, 0xFFC9C6B5, 0xFF9A9886)
    private val paperLight = PaletteSeed(0xFF8A5A2B, 0xFFFFFFFF, 0xFFF6EEDC, 0xFF2B2118, 0xFFE3D3B0, 0xFF5A4A3A, 0xFF6E5D4A)
    private val paperDark = PaletteSeed(0xFFD9A566, 0xFF2B1A05, 0xFF1F1A14, 0xFFEFE6D6, 0xFF3B3023, 0xFFCDBFA8, 0xFF9C8D78)
    private val oceanLight = PaletteSeed(0xFF1F6F9F, 0xFFFFFFFF, 0xFFF2F7FA, 0xFF14232E, 0xFFC9DCE8, 0xFF3F5666, 0xFF4F6B7C)
    private val oceanDark = PaletteSeed(0xFF6DB7E3, 0xFF00253A, 0xFF101A21, 0xFFE3EEF5, 0xFF223744, 0xFFB5C8D6, 0xFF8197A6)
    private val forestLight = PaletteSeed(0xFF2F7A3D, 0xFFFFFFFF, 0xFFF3F7F0, 0xFF17251A, 0xFFCFE0C4, 0xFF43563F, 0xFF56694F)
    private val forestDark = PaletteSeed(0xFF7FCB8A, 0xFF003A12, 0xFF121A14, 0xFFE3EEE2, 0xFF27392B, 0xFFB6CBB6, 0xFF86A087)
    private val sakuraLight = PaletteSeed(0xFFB83C65, 0xFFFFFFFF, 0xFFFFF6F7, 0xFF2B1519, 0xFFF4CCD6, 0xFF5E3E45, 0xFF7A5560)
    private val sakuraDark = PaletteSeed(0xFFF29DB6, 0xFF4A0A25, 0xFF1E1317, 0xFFF5E4E8, 0xFF43242E, 0xFFD6B7C0, 0xFFA88893)

    /** As cores-base de uma paleta fixa; `null` para [AppPalette.SYSTEM], que vem do sistema. */
    fun seed(palette: AppPalette, dark: Boolean): PaletteSeed? = when (palette) {
        AppPalette.MADOE -> if (dark) madoeDark else madoeLight
        AppPalette.PAPER -> if (dark) paperDark else paperLight
        AppPalette.OCEAN -> if (dark) oceanDark else oceanLight
        AppPalette.FOREST -> if (dark) forestDark else forestLight
        AppPalette.SAKURA -> if (dark) sakuraDark else sakuraLight
        AppPalette.SYSTEM -> null
    }
}

private fun channel(value: Int): Double {
    val c = value / 255.0
    return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

private fun luminance(argb: Long): Double {
    val r = ((argb shr 16) and 0xFF).toInt()
    val g = ((argb shr 8) and 0xFF).toInt()
    val b = (argb and 0xFF).toInt()
    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
}

/** Razão de contraste WCAG entre duas cores ARGB (de 1 a 21). */
fun contrastRatio(first: Long, second: Long): Double {
    val a = luminance(first)
    val b = luminance(second)
    return (max(a, b) + 0.05) / (min(a, b) + 0.05)
}
