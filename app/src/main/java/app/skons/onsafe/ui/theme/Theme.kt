package app.skons.onsafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AppColors.Red,
    background = AppColors.BgLight,
    surface = AppColors.CardLight,
    onPrimary = Color.White,
    onBackground = AppColors.TextLight,
    onSurface = AppColors.TextLight,
)

private val DarkColors = darkColorScheme(
    primary = AppColors.RedDark,
    background = AppColors.BgDark,
    surface = AppColors.CardDark,
    onPrimary = Color.White,
    onBackground = AppColors.TextDark,
    onSurface = AppColors.TextDark,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            content()
        }
    }
}
