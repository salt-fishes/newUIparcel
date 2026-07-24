package com.chenran.parcel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.chenran.parcel.util.ThemeManager

val LocalIsDarkTheme = compositionLocalOf { false }

// ---------------------------------------------------------------
// v2.0 四套 ColorScheme（R9，PRD v2.0 §5.1）
// primary 全主题统一 emerald 系；面板差异由 LocalParcelPalette 承载
// ---------------------------------------------------------------

private val LightColorScheme = lightColorScheme(
    primary = Emerald500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F3D0),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = LightIndigoText,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E7FF),
    onTertiaryContainer = LightIndigoText,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Slate100,
    onBackground = Slate900,
    surface = LightThemeCard,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Slate400,
    outlineVariant = LightThemeBorder,
    surfaceDim = Slate300,
    surfaceBright = LightThemeCard,
    surfaceContainerLowest = LightThemeCard,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate100,
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Slate300,
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Slate950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Slate400,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,
    tertiary = Indigo300,
    onTertiary = Slate950,
    tertiaryContainer = Color(0xFF3730A3),
    onTertiaryContainer = Color(0xFFE0E7FF),
    error = Rose300,
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate800,
    surfaceDim = Slate950,
    surfaceBright = Slate700,
    surfaceContainerLowest = Slate950,
    surfaceContainerLow = Slate900,
    surfaceContainer = Slate800,
    surfaceContainerHigh = DarkThemeChip,
    surfaceContainerHighest = Slate700,
)

private val EmeraldColorScheme = darkColorScheme(
    primary = Emerald300,
    onPrimary = Slate950,
    primaryContainer = EmeraldThemeCard,
    onPrimaryContainer = EmeraldThemeText,
    secondary = EmeraldThemeTextSub,
    onSecondary = Slate950,
    secondaryContainer = EmeraldThemeChip,
    onSecondaryContainer = EmeraldThemeText,
    tertiary = Amber300,
    onTertiary = Slate950,
    tertiaryContainer = EmeraldThemeChip,
    onTertiaryContainer = Amber300,
    error = Rose300,
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Slate950,
    onBackground = EmeraldThemeText,
    surface = EmeraldThemePanel,
    onSurface = EmeraldThemeText,
    surfaceVariant = EmeraldThemeCard,
    onSurfaceVariant = EmeraldThemeTextSub,
    outline = EmeraldThemePanelBorder,
    outlineVariant = EmeraldThemeChip,
    surfaceDim = Emerald950,
    surfaceBright = EmeraldThemeCard,
    surfaceContainerLowest = Emerald950,
    surfaceContainerLow = EmeraldThemePanel,
    surfaceContainer = EmeraldThemeCard,
    surfaceContainerHigh = EmeraldThemeChip,
    surfaceContainerHighest = EmeraldThemePanelBorder,
)

private val VioletColorScheme = darkColorScheme(
    primary = Emerald300,
    onPrimary = Slate950,
    primaryContainer = VioletThemeCard,
    onPrimaryContainer = VioletThemeText,
    secondary = VioletThemeTextSub,
    onSecondary = Slate950,
    secondaryContainer = VioletThemeChip,
    onSecondaryContainer = VioletThemeText,
    tertiary = Purple300,
    onTertiary = Slate950,
    tertiaryContainer = VioletThemeChip,
    onTertiaryContainer = VioletThemeTextSub,
    error = Rose300,
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Indigo950,
    onBackground = VioletThemeText,
    surface = VioletThemePanel,
    onSurface = VioletThemeText,
    surfaceVariant = VioletThemeCard,
    onSurfaceVariant = VioletThemeTextSub,
    outline = VioletThemePanelBorder,
    outlineVariant = VioletThemeChip,
    surfaceDim = Slate950,
    surfaceBright = VioletThemeCard,
    surfaceContainerLowest = Slate950,
    surfaceContainerLow = VioletThemePanel,
    surfaceContainer = VioletThemeCard,
    surfaceContainerHigh = VioletThemeChip,
    surfaceContainerHighest = VioletThemePanelBorder,
)

/**
 * v2.0 主题入口：4 套主题 + 跟随系统（决策 D6）。
 * 页面渐变背景由 ThemeBackdrop 渲染；壁纸开启时覆盖渐变（对齐 Web）。
 */
@Composable
fun ParcelTheme(
    themeMode: Int = ThemeManager.MODE_SYSTEM,
    isSeniorMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val resolvedMode = if (themeMode == ThemeManager.MODE_SYSTEM) {
        if (systemDark) ThemeManager.MODE_DARK else ThemeManager.MODE_LIGHT
    } else {
        themeMode
    }
    val isDark = resolvedMode != ThemeManager.MODE_LIGHT
    val colorScheme = when (resolvedMode) {
        ThemeManager.MODE_LIGHT -> LightColorScheme
        ThemeManager.MODE_EMERALD -> EmeraldColorScheme
        ThemeManager.MODE_VIOLET -> VioletColorScheme
        else -> DarkColorScheme
    }
    val palette = when (resolvedMode) {
        ThemeManager.MODE_LIGHT -> LightParcelPalette
        ThemeManager.MODE_EMERALD -> EmeraldParcelPalette
        ThemeManager.MODE_VIOLET -> VioletParcelPalette
        else -> DarkParcelPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(isSeniorMode),
        shapes = ParcelShapes,
        content = {
            CompositionLocalProvider(
                LocalIsDarkTheme provides isDark,
                LocalParcelPalette provides palette,
            ) {
                content()
            }
        }
    )
}
