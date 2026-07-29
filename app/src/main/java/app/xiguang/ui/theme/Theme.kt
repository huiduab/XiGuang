package app.xiguang.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = OxidizedCopper,
    onPrimary = WarmMist,
    secondary = MineralBlue,
    tertiary = Sage,
    background = WarmMist,
    onBackground = Ink,
    surface = WarmSurface,
    onSurface = Ink,
    surfaceVariant = WarmSurface,
    onSurfaceVariant = MutedInk,
    outline = Hairline,
)

private val ColorDarkCopper = androidx.compose.ui.graphics.Color(0xFFE49B68)
private val ColorDarkBlue = androidx.compose.ui.graphics.Color(0xFF87A9CA)
private val ColorDarkSage = androidx.compose.ui.graphics.Color(0xFF9DB18D)

private val DarkColors = darkColorScheme(
    primary = ColorDarkCopper,
    onPrimary = Graphite,
    secondary = ColorDarkBlue,
    tertiary = ColorDarkSage,
    background = Graphite,
    onBackground = SoftIvory,
    surface = GraphiteSurface,
    onSurface = SoftIvory,
    surfaceVariant = GraphiteSurface,
    onSurfaceVariant = DarkMuted,
    outline = DarkHairline,
)

@Composable
fun XiguangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = XiguangTypography,
        content = content,
    )
}
