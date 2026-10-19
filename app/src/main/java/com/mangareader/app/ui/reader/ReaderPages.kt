package com.mangareader.app.ui.reader

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mangareader.app.R
import com.mangareader.app.data.model.FitMode
import kotlin.math.abs
import kotlin.math.max

private const val MAX_SCALE = 5f

internal sealed interface PageResult {
    data object Loading : PageResult
    data class Ready(val bitmap: Bitmap) : PageResult
    data object Failed : PageResult
}

@Composable
private fun rememberPageResult(
    loader: PageLoader,
    index: Int,
    maxWidth: Int,
    maxHeight: Int,
    retry: Int,
): State<PageResult> = produceState<PageResult>(PageResult.Loading, loader, index, maxWidth, maxHeight, retry) {
    val bitmap = loader.load(index, maxWidth, maxHeight)
    value = if (bitmap != null) PageResult.Ready(bitmap) else PageResult.Failed
}

private fun nightFilter(enabled: Boolean): ColorFilter? =
    if (enabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.65f, 0.65f, 0.65f, 1f) }) else null

/**
 * Uma página no modo "página a página": ajuste à largura ou à altura, zoom por pinça e
 * arrasto para ver as partes que sobram. Quando o arrasto chega na borda, o gesto é devolvido
 * ao `HorizontalPager`, que então vira a página.
 */
@Composable
internal fun ZoomablePage(
    loader: PageLoader,
    index: Int,
    fit: FitMode,
    startAtRight: Boolean,
    night: Boolean,
    onTap: (x: Float, width: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val cw = constraints.maxWidth.toFloat()
        val ch = constraints.maxHeight.toFloat()
        var retry by remember { mutableIntStateOf(0) }
        val result by rememberPageResult(loader, index, (cw * 1.5f).toInt(), (ch * 1.5f).toInt(), retry)

        when (val r = result) {
            PageResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            PageResult.Failed -> PageError(onRetry = { retry++ })
            is PageResult.Ready -> ZoomableBitmap(r.bitmap, index, cw, ch, fit, startAtRight, night, onTap)
        }
    }
}

@Composable
private fun ZoomableBitmap(
    bitmap: Bitmap,
    index: Int,
    cw: Float,
    ch: Float,
    fit: FitMode,
    startAtRight: Boolean,
    night: Boolean,
    onTap: (Float, Int) -> Unit,
) {
    val density = LocalDensity.current
    val aspect = bitmap.height.toFloat() / bitmap.width.toFloat()
    val baseW = if (fit == FitMode.WIDTH) cw else ch / aspect
    val baseH = if (fit == FitMode.WIDTH) cw * aspect else ch

    // Começa no topo da página e, se sobrar largura, na borda por onde a leitura começa.
    val startOffset = Offset(
        x = max(0f, (baseW - cw) / 2f) * if (startAtRight) -1f else 1f,
        y = max(0f, (baseH - ch) / 2f),
    )
    var scale by remember(bitmap, fit, cw, ch) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap, fit, cw, ch, startAtRight) { mutableStateOf(startOffset) }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val colorFilter = remember(night) { nightFilter(night) }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(cw, ch) {
                detectTapGestures(onTap = { onTap(it.x, size.width) })
            }
            .pointerInput(baseW, baseH, cw, ch) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        if (zoom != 1f || pan != Offset.Zero) {
                            val centroid = event.calculateCentroid(useCurrent = false)
                            val newScale = (scale * zoom).coerceIn(1f, MAX_SCALE)
                            val ratio = newScale / scale
                            val p = if (centroid.isSpecified) centroid - Offset(cw / 2f, ch / 2f) else Offset.Zero
                            val maxX = max(0f, (baseW * newScale - cw) / 2f)
                            val maxY = max(0f, (baseH * newScale - ch) / 2f)
                            val nx = (offset.x * ratio + p.x * (1f - ratio) + pan.x).coerceIn(-maxX, maxX)
                            val ny = (offset.y * ratio + p.y * (1f - ratio) + pan.y).coerceIn(-maxY, maxY)

                            val multiTouch = zoom != 1f || event.changes.count { it.pressed } >= 2
                            val horizontal = abs(pan.x) >= abs(pan.y)
                            val canMove = if (horizontal) nx != offset.x else ny != offset.y
                            // Só "toma" o gesto se a página ainda pode andar naquela direção;
                            // senão ele passa para o pager (virar a página).
                            if (multiTouch || canMove) {
                                scale = newScale
                                offset = Offset(nx, ny)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = stringResource(R.string.reader_page_description, index + 1),
            contentScale = ContentScale.FillBounds,
            colorFilter = colorFilter,
            modifier = Modifier
                .align(Alignment.Center)
                .requiredSize(with(density) { baseW.toDp() }, with(density) { baseH.toDp() })
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

@Composable
private fun PageError(onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.page_load_failed),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.retry))
        }
    }
}

/** Modo de rolagem contínua vertical: páginas empilhadas, sempre ajustadas à largura. */
@Composable
internal fun ContinuousReader(
    loader: PageLoader,
    pageCount: Int,
    listState: LazyListState,
    night: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) },
    ) {
        val width = constraints.maxWidth
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(count = pageCount, key = { it }) { index ->
                ContinuousPage(loader, index, width, night)
            }
        }
    }
}

@Composable
private fun ContinuousPage(loader: PageLoader, index: Int, width: Int, night: Boolean) {
    var retry by remember { mutableIntStateOf(0) }
    // maxHeight = 1: só a largura decide a resolução, porque a página é ajustada à largura.
    val result by rememberPageResult(loader, index, (width * 1.25f).toInt(), 1, retry)
    val colorFilter = remember(night) { nightFilter(night) }

    when (val r = result) {
        PageResult.Loading -> Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        PageResult.Failed -> Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
        ) { PageError(onRetry = { retry++ }) }
        is PageResult.Ready -> {
            val imageBitmap = remember(r.bitmap) { r.bitmap.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(R.string.reader_page_description, index + 1),
                contentScale = ContentScale.FillWidth,
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
