package app.skons.onsafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

val LocalDarkTheme = compositionLocalOf { false }

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

data class AppThemeColors(
    val text: Color,
    val sub: Color,
    val hint: Color,
    val border: Color,
    val cardBg: Color,
    val bg: Color,
    val blue: Color,
    val red: Color,
    val badgeBg: Color,
    val badgeTx: Color,
    val badgeBorder: Color,
)

@Composable
fun appThemeColors(): AppThemeColors {
    val isDark = LocalDarkTheme.current
    return remember(isDark) {
        AppThemeColors(
            text = if (isDark) AppColors.TextDark else AppColors.TextLight,
            sub = if (isDark) AppColors.SubDark else AppColors.SubLight,
            hint = if (isDark) AppColors.HintDark else AppColors.HintLight,
            border = if (isDark) AppColors.BorderDark else AppColors.BorderLight,
            cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight,
            bg = if (isDark) AppColors.BgDark else AppColors.BgLight,
            blue = if (isDark) AppColors.BlueDark else AppColors.Blue,
            red = if (isDark) AppColors.RedDark else AppColors.Red,
            badgeBg = if (isDark) AppColors.BadgeBgDark else AppColors.BadgeBgLight,
            badgeTx = if (isDark) AppColors.BadgeTxDark else AppColors.BadgeTxLight,
            badgeBorder = AppColors.AppBarYellow.copy(alpha = if (isDark) 0.4f else 0.55f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides null,
            LocalDarkTheme provides darkTheme,
        ) {
            content()
        }
    }
}
