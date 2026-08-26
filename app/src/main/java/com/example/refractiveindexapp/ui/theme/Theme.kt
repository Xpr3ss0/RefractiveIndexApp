package com.example.refractiveindexapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.refractiveindexapp.settings.ThemePreference
import com.example.refractiveindexapp.settings.ColorSchemePreference

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun IndexInfoTheme(
    themePreference: ThemePreference = ThemePreference.System,
    colorSchemePreference: ColorSchemePreference = ColorSchemePreference.System,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.System -> isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    val colorScheme = when {
        colorSchemePreference == ColorSchemePreference.System && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        colorSchemePreference == ColorSchemePreference.Ocean && darkTheme -> darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF89D0FF), secondary = androidx.compose.ui.graphics.Color(0xFFB8C8FF))
        colorSchemePreference == ColorSchemePreference.Ocean -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF006493), secondary = androidx.compose.ui.graphics.Color(0xFF405F90))
        colorSchemePreference == ColorSchemePreference.Forest && darkTheme -> darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF9CD49B), secondary = androidx.compose.ui.graphics.Color(0xFFB5C9A8))
        colorSchemePreference == ColorSchemePreference.Forest -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF276A2E), secondary = androidx.compose.ui.graphics.Color(0xFF4D6547))
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
