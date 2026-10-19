package com.mangareader.app.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mangareader.app.R
import com.mangareader.app.ui.components.halftoneEdge
import com.mangareader.app.ui.theme.Brick
import com.mangareader.app.ui.theme.Cream
import com.mangareader.app.ui.theme.Ink
import com.mangareader.app.ui.theme.Orange
import com.mangareader.app.ui.theme.Teal
import kotlinx.coroutines.launch

private data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val text: Int,
    val icon: ImageVector,
)

/** Introdução de 3 páginas, mostrada só na primeira abertura. A arte é desenhada com formas simples. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(R.string.onboarding_1_title, R.string.onboarding_1_text, Icons.Outlined.MenuBook),
            OnboardingPage(R.string.onboarding_2_title, R.string.onboarding_2_text, Icons.Outlined.FolderOpen),
            OnboardingPage(R.string.onboarding_3_title, R.string.onboarding_3_text, Icons.Outlined.CollectionsBookmark),
        )
    }
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(Teal)
            .halftoneEdge(Color.White.copy(alpha = 0.12f)),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(color = Brick, shape = CircleShape) {
                    IconButton(onClick = onFinish) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.onboarding_skip),
                            tint = Color.White,
                        )
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                PageContent(pages[index])
            }

            Row(
                Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(pages.size) { index ->
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(
                                if (index == pagerState.currentPage) Orange else Color.White.copy(alpha = 0.4f),
                                CircleShape,
                            ),
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Ink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    stringResource(if (isLast) R.string.onboarding_start else R.string.onboarding_next),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(page.text),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
        )
        Art(page.icon)
    }
}

/** Três "capas" de quadrinho inclinadas com um ícone no centro. */
@Composable
private fun Art(icon: ImageVector) {
    Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        ArtCard(Orange, -12f, 100.dp, 140.dp, Modifier.offset(x = (-52).dp, y = (-18).dp))
        ArtCard(Color(0xFF5B8DB8), 9f, 100.dp, 140.dp, Modifier.offset(x = 52.dp, y = 24.dp))
        ArtCard(Cream, -3f, 120.dp, 160.dp, Modifier) {
            Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.size(0.dp))
    }
}

@Composable
private fun ArtCard(
    color: Color,
    rotation: Float,
    width: Dp,
    height: Dp,
    modifier: Modifier,
    content: @Composable () -> Unit = {},
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .size(width, height)
            .rotate(rotation)
            .background(color, shape)
            .border(3.dp, Ink, shape),
        contentAlignment = Alignment.Center,
    ) { content() }
}
