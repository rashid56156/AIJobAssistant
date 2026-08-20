package com.sample.aijobassistant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = SlateDark,
    secondary = AmberDark,
    background = SlateDark,
    onBackground = OnSlateDark,
    surface = SlateMedium,
    onSurface = OnSlateDark
)

private val LightColors = lightColorScheme(
    primary = AmberDark,
    onPrimary = SlateLight,
    secondary = Amber,
    background = SlateLight,
    onBackground = OnSlateLight,
    surface = Color(0xFFFFFFFF),
    onSurface = OnSlateLight
)

@Composable
fun AIJobAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // off by default: this app's identity is the slate/amber palette, not per-device wallpaper colors
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
