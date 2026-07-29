package app.xiguang.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class XiguangAccentColors(
    val mineralBlue: Color,
    val oxidizedCopper: Color,
    val sage: Color,
    val plum: Color,
)

@Immutable
data class XiguangThemeDefinition(
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val lightAccents: XiguangAccentColors,
    val darkAccents: XiguangAccentColors,
    val typography: Typography,
)

internal val LocalXiguangAccentColors = staticCompositionLocalOf {
    XiguangThemeCatalog.default.lightAccents
}

val MaterialTheme.xiguangAccents: XiguangAccentColors
    @Composable
    @ReadOnlyComposable
    get() = LocalXiguangAccentColors.current
