
package com.example.glassfinance.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF20352B)
val BrandBlue = Color(0xFF176B50)

@Immutable
data class FinanceColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
    val onHeroMuted: Color
)

val LightFinanceColors = FinanceColors(
    income = Color(0xFF25845E),
    expense = Color(0xFFD95757),
    warning = Color(0xFFE8B65A),
    backgroundTop = Color(0xFFF4F1E9),
    backgroundBottom = Color(0xFFF9F8F4),
    heroStart = Color(0xFF155B43),
    heroEnd = Color(0xFF278363),
    onHero = Color.White,
    onHeroMuted = Color(0xFFD5E9DF)
)

val DarkFinanceColors = FinanceColors(
    income = Color(0xFF69D6A0),
    expense = Color(0xFFFF8585),
    warning = Color(0xFFFFCB70),
    backgroundTop = Color(0xFF17251F),
    backgroundBottom = Color(0xFF101914),
    heroStart = Color(0xFF164C38),
    heroEnd = Color(0xFF236D50),
    onHero = Color.White,
    onHeroMuted = Color(0xFFD0E8DA)
)

val LocalFinanceColors = staticCompositionLocalOf {
    LightFinanceColors
}

val ChartPalette = listOf(
    Color(0xFF25845E),
    Color(0xFFE8B65A),
    Color(0xFFD95757),
    Color(0xFF5489A6),
    Color(0xFF8B78B8),
    Color(0xFF75A99A),
    Color(0xFFCB8B65),
    Color(0xFF94A3A0)
)
