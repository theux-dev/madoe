package com.mangareader.app.data

import com.mangareader.app.data.model.AppPalette
import com.mangareader.app.ui.theme.PaletteSeed
import com.mangareader.app.ui.theme.Palettes
import com.mangareader.app.ui.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Confere, para cada paleta fixa nos modos claro e escuro, o contraste dos pares de cores usados na interface. */
class PaletteContrastTest {
    private val fixed = AppPalette.entries.filter { it != AppPalette.SYSTEM }

    private fun forEachSeed(check: (AppPalette, Boolean, PaletteSeed) -> Unit) {
        for (palette in fixed) for (dark in listOf(false, true)) check(palette, dark, Palettes.seed(palette, dark)!!)
    }

    private fun assertContrast(minimum: Double, what: String, palette: AppPalette, dark: Boolean, a: Long, b: Long) {
        val ratio = contrastRatio(a, b)
        assertTrue(
            "$what em ${palette.name} (${if (dark) "escuro" else "claro"}): contraste %.2f, esperado pelo menos $minimum".format(ratio),
            ratio >= minimum,
        )
    }

    @Test fun everyFixedPaletteHasLightAndDarkColors() {
        fixed.forEach {
            assertNotNull(Palettes.seed(it, false))
            assertNotNull(Palettes.seed(it, true))
        }
        assertNull(Palettes.seed(AppPalette.SYSTEM, false))
    }

    @Test fun textIsReadableOnBackgroundAndCards() = forEachSeed { p, dark, s ->
        assertContrast(4.5, "texto sobre o fundo", p, dark, s.onBackground, s.background)
        assertContrast(4.5, "texto sobre o cartão", p, dark, s.onBackground, s.card)
        assertContrast(4.5, "texto secundário sobre o cartão", p, dark, s.onCard, s.card)
        assertContrast(4.5, "texto secundário sobre o fundo", p, dark, s.onCard, s.background)
    }

    @Test fun textOnAccentButtonsIsReadable() = forEachSeed { p, dark, s ->
        assertContrast(4.5, "texto sobre a cor de destaque", p, dark, s.onPrimary, s.primary)
    }

    @Test fun accentIsVisibleAgainstTheBackground() = forEachSeed { p, dark, s ->
        assertContrast(3.0, "destaque sobre o fundo", p, dark, s.primary, s.background)
    }

    @Test fun accentIsVisibleAgainstCards() = forEachSeed { p, dark, s ->
        // A paleta original, no modo claro, segue as referências de design (verde-azulado sobre verde-oliva)
        // e fica um pouco abaixo de 3:1 nas barras de progresso. As paletas novas respeitam o mínimo.
        val knownException = p == AppPalette.MADOE && !dark
        if (!knownException) assertContrast(3.0, "destaque sobre o cartão", p, dark, s.primary, s.card)
    }

    @Test fun contrastRatioMatchesKnownValues() {
        assertEquals(21.0, contrastRatio(0xFF000000, 0xFFFFFFFF), 0.01)
        assertEquals(1.0, contrastRatio(0xFF3E7C78, 0xFF3E7C78), 0.0001)
    }
}
