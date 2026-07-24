package com.chenran.parcel.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.chenran.parcel.ui.theme.parcelPalette

/**
 * 组件层语义取色（函数签名与 v1 完全一致，调用方零改动）。
 * v2.0 起实现改读 LocalParcelPalette（4 主题语义调色板），
 * 替代 v1 的 M3 surface 直读。
 */
@Composable
fun parcelBaseColor(): Color = parcelPalette.panel

@Composable
fun parcelOnCardColor(): Color = parcelPalette.textPrimary

@Composable
fun parcelOnCardVariantColor(): Color = parcelPalette.textSecondary

@Composable
fun parcelSheetColor(): Color = parcelPalette.panel

@Composable
fun parcelButtonColor(): Color = parcelPalette.chip

@Composable
fun parcelChipColor(): Color = parcelPalette.chip
