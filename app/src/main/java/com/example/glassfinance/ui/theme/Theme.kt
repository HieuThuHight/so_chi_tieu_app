
package com.example.glassfinance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDE4),
    onPrimaryContainer = Navy,

    secondary = Color(0xFF648273),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2ECE5),
    onSecondaryContainer = Color(0xFF263B30),

    tertiary = Color(0xFFB98243),
    onTertiary = Color.White,

    background = Color(0xFFF4F1E9),
    onBackground = Color(0xFF202D27),

    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF202D27),

    surfaceVariant = Color(0xFFEAE9E1),
    onSurfaceVariant = Color(0xFF68736C),

    outline = Color(0xFF9BA99F),
    outlineVariant = Color(0xFFDCE2DA),

    error = Color(0xFFD95757),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF9F5),
    surfaceContainer = Color(0xFFF1F0E9),
    surfaceContainerHigh = Color(0xFFEAEAE2),
    surfaceContainerHighest = Color(0xFFE2E5DC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF79C9A2),
    onPrimary = Color(0xFF123D2C),
    primaryContainer = Color(0xFF205B43),
    onPrimaryContainer = Color(0xFFD6F2E1),

    secondary = Color(0xFFA8C6B5),
    onSecondary = Color(0xFF263B30),
    secondaryContainer = Color(0xFF354D40),
    onSecondaryContainer = Color(0xFFDCEBE1),

    tertiary = Color(0xFFE4B878),
    onTertiary = Color(0xFF402A10),

    background = Color(0xFF151D18),
    onBackground = Color(0xFFE7EEE8),

    surface = Color(0xFF1C2620),
    onSurface = Color(0xFFE7EEE8),

    surfaceVariant = Color(0xFF303D34),
    onSurfaceVariant = Color(0xFFB7C7BB),

    outline = Color(0xFF84968A),
    outlineVariant = Color(0xFF3D4B41),

    error = Color(0xFFFF8585),

    surfaceContainerLowest = Color(0xFF111813),
    surfaceContainerLow = Color(0xFF19231C),
    surfaceContainer = Color(0xFF202C24),
    surfaceContainerHigh = Color(0xFF29362D),
    surfaceContainerHighest = Color(0xFF334138)
)

object FinanceTheme {
    val colors: FinanceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinanceColors.current
}

@Composable
fun GlassFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val financeColors = if (darkTheme) {
        DarkFinanceColors
    } else {
        LightFinanceColors
    }

    CompositionLocalProvider(
        LocalFinanceColors provides financeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
