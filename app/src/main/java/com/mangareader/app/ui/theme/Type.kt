package com.mangareader.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.mangareader.app.R

/** Fonte de exibição (Bowlby One, licença OFL) embutida no app para funcionar offline. */
val DisplayFont = FontFamily(Font(R.font.bowlby_one))

private val Base = Typography()

val AppTypography = Typography(
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = Base.titleMedium,
)
