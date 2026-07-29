package app.xiguang.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun XiguangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    definition: XiguangThemeDefinition = XiguangThemeCatalog.default,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) definition.darkColors else definition.lightColors
    val accents = if (darkTheme) definition.darkAccents else definition.lightAccents

    CompositionLocalProvider(LocalXiguangAccentColors provides accents) {
        MaterialTheme(
            colorScheme = colors,
            typography = definition.typography,
            content = content,
        )
    }
}
