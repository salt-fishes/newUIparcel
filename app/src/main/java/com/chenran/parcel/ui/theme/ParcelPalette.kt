package com.chenran.parcel.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * v2.0 Web 对齐语义调色板（R9）。
 * M3 ColorScheme 没有"面板/卡片/徽章强调色"槽位，这里以扩展 Local 承载，
 * 色值直译 Web 参考版 4 套主题（见 docs/PRD.md §5.1）。
 *
 * 面板/卡片/芯片均为不透明纯色（决策 D1，无玻璃）；徽章底色/边框用强调色
 * 低透明度合成（与 Web 徽章语言一致），文字用浅色/深色变体。
 */
class ParcelPalette(
    // 页面渐变背景（主题标识）
    val pageGradientStart: Color,
    val pageGradientMid: Color,
    val pageGradientEnd: Color,
    // 面板族（不透明）：顶栏/底栏/弹层、卡片、小色块、细边框
    val panel: Color,
    val card: Color,
    val chip: Color,
    val border: Color,
    // 文字层级
    val textPrimary: Color,
    val textSecondary: Color,
    val textWeak: Color,
    // 语义强调色（徽章文字/图标/选中态）
    val emerald: Color,
    val amber: Color,
    val purple: Color,
    val indigo: Color,
    val rose: Color,
    val sky: Color,
    val blue: Color,
    val orange: Color,
    // FAB（emerald→teal 渐变）与其上文字
    val fabStart: Color,
    val fabEnd: Color,
    val onFab: Color,
) {
    /** 徽章底色：强调色 18% 合成（Web bg-{c}-500/15~20）。 */
    fun badgeBg(accent: Color): Color = accent.copy(alpha = 0.18f)

    /** 徽章边框：强调色 35% 合成（Web border-{c}-500/30~40）。 */
    fun badgeBorder(accent: Color): Color = accent.copy(alpha = 0.35f)
}

val DarkParcelPalette = ParcelPalette(
    pageGradientStart = Slate900,
    pageGradientMid = Slate950,
    pageGradientEnd = Indigo950,
    panel = Slate900,
    card = Slate800,
    chip = DarkThemeChip,
    border = Slate700,
    textPrimary = Slate100,
    textSecondary = Slate400,
    textWeak = Slate500,
    emerald = Emerald400,
    amber = Amber400,
    purple = Purple300,
    indigo = Indigo300,
    rose = Rose300,
    sky = Sky300,
    blue = Blue400,
    orange = Orange400,
    fabStart = Emerald400,
    fabEnd = Teal500,
    onFab = Slate950,
)

val LightParcelPalette = ParcelPalette(
    pageGradientStart = Slate100,
    pageGradientMid = Emerald50,
    pageGradientEnd = Teal50,
    panel = LightThemeCard,
    card = LightThemeCard,
    chip = LightThemeChip,
    border = LightThemeBorder,
    textPrimary = Slate900,
    textSecondary = Color(0xFF475569),
    textWeak = Slate500,
    emerald = LightEmeraldText,
    amber = LightAmberText,
    purple = LightPurpleText,
    indigo = LightIndigoText,
    rose = LightRoseText,
    sky = LightSkyText,
    blue = LightBlueText,
    orange = LightOrangeText,
    fabStart = Emerald500,
    fabEnd = Teal500,
    onFab = Color.White,
)

val EmeraldParcelPalette = ParcelPalette(
    pageGradientStart = Slate950,
    pageGradientMid = Emerald950,
    pageGradientEnd = Teal950,
    panel = EmeraldThemePanel,
    card = EmeraldThemeCard,
    chip = EmeraldThemeChip,
    border = EmeraldThemePanelBorder,
    textPrimary = EmeraldThemeText,
    textSecondary = EmeraldThemeTextSub,
    textWeak = Color(0xFF6EE7B7),
    emerald = Emerald300,
    amber = Amber300,
    purple = Purple300,
    indigo = Indigo300,
    rose = Rose300,
    sky = Sky300,
    blue = Color(0xFF93C5FD),
    orange = Color(0xFFFDBA74),
    fabStart = Emerald400,
    fabEnd = Teal500,
    onFab = Slate950,
)

val VioletParcelPalette = ParcelPalette(
    pageGradientStart = Slate950,
    pageGradientMid = Indigo950,
    pageGradientEnd = Purple950,
    panel = VioletThemePanel,
    card = VioletThemeCard,
    chip = VioletThemeChip,
    border = VioletThemePanelBorder,
    textPrimary = VioletThemeText,
    textSecondary = VioletThemeTextSub,
    textWeak = Color(0xFFC4B5FD),
    emerald = Emerald300,
    amber = Amber300,
    purple = Color(0xFFE9D5FF),
    indigo = Color(0xFFC7D2FE),
    rose = Rose300,
    sky = Sky300,
    blue = Color(0xFF93C5FD),
    orange = Color(0xFFFDBA74),
    fabStart = Emerald400,
    fabEnd = Teal500,
    onFab = Slate950,
)

val LocalParcelPalette = staticCompositionLocalOf<ParcelPalette> { DarkParcelPalette }

/** 当前主题语义调色板（组件统一经此取色）。 */
val parcelPalette: ParcelPalette
    @Composable get() = LocalParcelPalette.current
