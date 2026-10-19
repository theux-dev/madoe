package com.mangareader.app.data

import com.mangareader.app.ui.reader.clampTranslation
import com.mangareader.app.ui.reader.focusedTranslation
import com.mangareader.app.ui.reader.zoomQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class ZoomMathTest {
    @Test fun focusAtTheReferencePointNeverMoves() {
        // Sem arrasto, ampliar sobre o próprio ponto de referência não desloca nada.
        assertEquals(0f, focusedTranslation(translation = 0f, focus = 0f, ratio = 3f), 0.0001f)
    }

    @Test fun pointUnderTheFingersStaysPut() {
        // Ponto a 100 px do centro, zoom de 1x para 2x: a tela passa a mostrar a imagem deslocada
        // de -100 px, de modo que o ponto original continue sob os dedos.
        assertEquals(-100f, focusedTranslation(translation = 0f, focus = 100f, ratio = 2f), 0.0001f)
    }

    @Test fun zoomingOutBringsTranslationBack() {
        val zoomedIn = focusedTranslation(0f, 100f, 2f)
        assertEquals(0f, focusedTranslation(zoomedIn, 100f, 0.5f), 0.0001f)
    }

    @Test fun translationIsClamped() {
        assertEquals(50f, clampTranslation(80f, 50f), 0f)
        assertEquals(-50f, clampTranslation(-80f, 50f), 0f)
        assertEquals(0f, clampTranslation(10f, 0f), 0f)
    }

    @Test fun qualityRisesWithZoom() {
        assertEquals(1.5f, zoomQuality(1f), 0f)
        assertEquals(3f, zoomQuality(2f), 0f)
    }
}
