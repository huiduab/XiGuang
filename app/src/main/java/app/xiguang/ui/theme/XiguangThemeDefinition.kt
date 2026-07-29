package app.xiguang.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

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

private val DarkCopper = Color(0xFFE49B68)
private val DarkBlue = Color(0xFF87A9CA)
private val DarkSage = Color(0xFF9DB18D)
private val DarkPlum = Color(0xFFC59AAF)

private val DarkColors = darkColorScheme(
    primary = DarkCopper,
    onPrimary = Graphite,
    secondary = DarkBlue,
    tertiary = DarkSage,
    background = Graphite,
    onBackground = SoftIvory,
    surface = GraphiteSurface,
    onSurface = SoftIvory,
    surfaceVariant = GraphiteSurface,
    onSurfaceVariant = DarkMuted,
    outline = DarkHairline,
)

val DefaultXiguangTheme = XiguangThemeDefinition(
    lightColors = LightColors,
    darkColors = DarkColors,
    lightAccents = XiguangAccentColors(
        mineralBlue = MineralBlue,
        oxidizedCopper = OxidizedCopper,
        sage = Sage,
        plum = Plum,
    ),
    darkAccents = XiguangAccentColors(
        mineralBlue = DarkBlue,
        oxidizedCopper = DarkCopper,
        sage = DarkSage,
        plum = DarkPlum,
    ),
    typography = XiguangTypography,
)

object XiguangThemeCatalog {
    val default: XiguangThemeDefinition = DefaultXiguangTheme
}
