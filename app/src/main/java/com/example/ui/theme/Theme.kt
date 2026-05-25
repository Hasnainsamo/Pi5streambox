package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SleekLightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SleekBackground,
    primaryContainer = SleekSecondary,
    onPrimaryContainer = SleekDeepDark,
    secondary = SleekPrimary,
    onSecondary = SleekBackground,
    background = SleekBackground,
    onBackground = SleekText,
    surface = SleekTabBg,
    onSurface = SleekText,
    surfaceVariant = SleekCardBg,
    onSurfaceVariant = SleekTextMedium,
    outline = SleekCardBorder,
    outlineVariant = SleekTabBorder
)

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekSecondary,
    onPrimary = SleekDeepDark,
    primaryContainer = SleekPrimary,
    onPrimaryContainer = SleekSecondary,
    secondary = SleekSecondary,
    onSecondary = SleekDeepDark,
    background = SleekDeepDark,
    onBackground = SleekBackground,
    surface = SleekDeepDark,
    onSurface = SleekBackground,
    surfaceVariant = SleekPrimary,
    onSurfaceVariant = SleekSecondary,
    outline = SleekPrimary,
    outlineVariant = SleekPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color by default to preserve the unique custom matching "Sleek Interface" branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SleekDarkColorScheme
        else -> SleekLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
