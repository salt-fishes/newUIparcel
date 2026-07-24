package com.chenran.parcel.util

import android.content.Context
import androidx.core.content.edit

object ThemeManager {
    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2
    const val MODE_EMERALD = 3
    const val MODE_VIOLET = 4

    private const val PREFS_NAME = "parcel_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): Int {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, MODE_SYSTEM)
        } catch (_: Exception) { MODE_SYSTEM }
    }

    fun setThemeMode(context: Context, mode: Int) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit { putInt(KEY_THEME_MODE, mode) }
        } catch (_: Exception) { }
    }

    fun modeLabel(mode: Int): String = when (mode) {
        MODE_SYSTEM -> "跟随系统"
        MODE_LIGHT -> "日间明亮"
        MODE_DARK -> "经典深色"
        MODE_EMERALD -> "翡翠夜景"
        MODE_VIOLET -> "极光紫罗兰"
        else -> "跟随系统"
    }
}
