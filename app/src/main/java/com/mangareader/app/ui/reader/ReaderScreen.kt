package com.mangareader.app.ui.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangareader.app.R
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class ReaderDialog { None, Settings, GoTo, Bookmarks, Info }

@Composable
fun ReaderScreen(onBack: () -> Unit, viewModel: ReaderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    ReaderWindowEffects(settings.orientation)

    when (val current = state) {
        ReaderState.Loading -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        is ReaderState.Error -> Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.reader_error_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                current.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Text(
                stringResource(R.string.reader_error_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Button(onClick = onBack) { Text(stringResource(R.string.back)) }
            OutlinedButton(
                onClick = { viewModel.removeFromLibrary(onBack) },
                modifier = Modifier.padding(top = 8.dp),
            ) { Text(stringResource(R.string.menu_remove_library)) }
        }

        is ReaderState.Ready -> ReaderContent(current, settings, viewModel, onBack)
    }
}

/** Orientação escolhida e tela sempre acesa durante a leitura; restaura tudo ao sair. */
@Composable
private fun ReaderWindowEffects(orientation: OrientationPref) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()

    DisposableEffect(orientation) {
        activity?.requestedOrientation = when (orientation) {
            OrientationPref.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            OrientationPref.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            OrientationPref.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
private fun ImmersiveEffect(controlsVisible: Boolean) {
    val view = LocalView.current
    val window = LocalContext.current.findActivity()?.window
    DisposableEffect(controlsVisible) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (controlsVisible) {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { }
    }
    // Ao sair do leitor, as barras do sistema voltam.
    DisposableEffect(Unit) {
        onDispose {
            window?.let { WindowCompat.getInsetsController(it, view).show(WindowInsetsCompat.Type.systemBars()) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReaderContent(
    ready: ReaderState.Ready,
    settings: AppSettings,
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
) {
    val loader = ready.loader
    val pageCount = loader.pageCount
    val rtl = settings.readingDirection == ReadingDirection.RTL
    val continuous = settings.readingMode == ReadingMode.CONTINUOUS
    val scope = rememberCoroutineScope()

    var currentPage by rememberSaveable { mutableStateOf(ready.startPage) }
    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    var dialog by rememberSaveable { mutableStateOf(ReaderDialog.None) }
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = ready.startPage) { pageCount }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = ready.startPage)

    ImmersiveEffect(controlsVisible)

    // Mantém a página atual em sincronia com o modo ativo e salva o progresso a cada troca.
    LaunchedEffect(continuous) {
        if (continuous) {
            if (listState.firstVisibleItemIndex != currentPage) listState.scrollToItem(currentPage)
            snapshotFlow { listState.firstVisibleItemIndex }.collect {
                currentPage = it
                viewModel.onPageChanged(it)
            }
        } else {
            if (pagerState.currentPage != currentPage) pagerState.scrollToPage(currentPage)
            snapshotFlow { pagerState.currentPage }.collect {
                currentPage = it
                viewModel.onPageChanged(it)
            }
        }
    }

    val goToPage: (Int) -> Unit = { target ->
        val page = target.coerceIn(0, pageCount - 1)
        scope.launch {
            if (continuous) listState.scrollToItem(page) else pagerState.animateScrollToPage(page)
        }
    }
    // `delta` segue a ordem de leitura: +1 é sempre a próxima página, em qualquer direção.
    val turn: (Int) -> Unit = { delta -> goToPage(currentPage + delta) }

    val onTapAt: (Float, Int) -> Unit = { x, width ->
        val third = width / 3f
        when {
            settings.pageNavigation == PageNavigation.GESTURES_ONLY -> controlsVisible = !controlsVisible
            x < third -> turn(if (rtl) 1 else -1)
            x > 2 * third -> turn(if (rtl) -1 else 1)
            else -> controlsVisible = !controlsVisible
        }
    }

    val background = if (settings.readerNightMode) Color(0xFF0E0F0C) else MaterialTheme.colorScheme.background

    Box(
        Modifier
            .fillMaxSize()
            .background(background),
    ) {
        if (continuous) {
            ContinuousReader(
                loader = loader,
                pageCount = pageCount,
                listState = listState,
                night = settings.readerNightMode,
                onTap = { controlsVisible = !controlsVisible },
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = rtl,
                beyondViewportPageCount = 1,
                userScrollEnabled = settings.pageNavigation != PageNavigation.TAP_ONLY,
            ) { page ->
                ZoomablePage(
                    loader = loader,
                    index = page,
                    fit = settings.fitMode,
                    startAtRight = rtl,
                    night = settings.readerNightMode,
                    onTap = onTapAt,
                )
            }
        }

        val bookmarkHere = bookmarks.firstOrNull { it.page == currentPage }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            ReaderTopBar(
                title = ready.comic.title,
                bookmarked = bookmarkHere != null,
                nightMode = settings.readerNightMode,
                onBack = onBack,
                onToggleBookmark = {
                    if (bookmarkHere != null) viewModel.removeBookmark(bookmarkHere.id) else viewModel.addBookmark(currentPage)
                },
                onOpenSettings = { dialog = ReaderDialog.Settings },
                onGoTo = { dialog = ReaderDialog.GoTo },
                onBookmarks = { dialog = ReaderDialog.Bookmarks },
                onToggleNight = { viewModel.setNightMode(!settings.readerNightMode) },
                onInfo = { dialog = ReaderDialog.Info },
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ReaderBottomBar(
                currentPage = currentPage,
                pageCount = pageCount,
                rtl = rtl,
                onGoTo = goToPage,
                onTurn = turn,
            )
        }
    }

    when (dialog) {
        ReaderDialog.None -> Unit
        ReaderDialog.Settings -> ReaderSettingsSheet(settings, viewModel, onDismiss = { dialog = ReaderDialog.None })
        ReaderDialog.GoTo -> GoToPageDialog(
            pageCount = pageCount,
            onGo = {
                dialog = ReaderDialog.None
                goToPage(it)
            },
            onDismiss = { dialog = ReaderDialog.None },
        )
        ReaderDialog.Bookmarks -> BookmarksDialog(
            bookmarks = bookmarks,
            currentPage = currentPage,
            onGo = {
                dialog = ReaderDialog.None
                goToPage(it)
            },
            onAdd = { viewModel.addBookmark(currentPage) },
            onRemove = viewModel::removeBookmark,
            onDismiss = { dialog = ReaderDialog.None },
        )
        ReaderDialog.Info -> InfoDialog(
            comic = ready.comic,
            currentPage = currentPage,
            pageCount = pageCount,
            onDismiss = { dialog = ReaderDialog.None },
        )
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    bookmarked: Boolean,
    nightMode: Boolean,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenSettings: () -> Unit,
    onGoTo: () -> Unit,
    onBookmarks: () -> Unit,
    onToggleNight: () -> Unit,
    onInfo: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.97f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(
                        if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add,
                    ),
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.reader_settings)) },
                        onClick = { menuOpen = false; onOpenSettings() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.go_to_page)) },
                        onClick = { menuOpen = false; onGoTo() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.bookmarks)) },
                        onClick = { menuOpen = false; onBookmarks() },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.night_mode) + if (nightMode) " ✓" else "",
                            )
                        },
                        onClick = { menuOpen = false; onToggleNight() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.information)) },
                        onClick = { menuOpen = false; onInfo() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    pageCount: Int,
    rtl: Boolean,
    onGoTo: (Int) -> Unit,
    onTurn: (Int) -> Unit,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val shownPage = (dragValue ?: currentPage.toFloat()).roundToInt().coerceIn(0, pageCount - 1)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.97f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.page_label, shownPage + 1, pageCount),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Os botões são "físicos": o da esquerda anda para trás em HQs e para frente em mangás.
                IconButton(onClick = { onTurn(if (rtl) 1 else -1) }) {
                    Icon(
                        Icons.Outlined.ChevronLeft,
                        contentDescription = stringResource(if (rtl) R.string.next_page else R.string.previous_page),
                    )
                }
                if (pageCount > 1) {
                    // No modo mangá o controle corre da direita para a esquerda, como a leitura.
                    CompositionLocalProvider(
                        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
                    ) {
                        Slider(
                            value = dragValue ?: currentPage.toFloat(),
                            onValueChange = { dragValue = it },
                            onValueChangeFinished = {
                                dragValue?.let { onGoTo(it.roundToInt()) }
                                dragValue = null
                            },
                            valueRange = 0f..(pageCount - 1).toFloat(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Box(Modifier.weight(1f))
                }
                IconButton(onClick = { onTurn(if (rtl) -1 else 1) }) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = stringResource(if (rtl) R.string.previous_page else R.string.next_page),
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
