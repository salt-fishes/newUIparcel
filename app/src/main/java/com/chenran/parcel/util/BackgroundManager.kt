package com.chenran.parcel.util

import android.content.Context
import androidx.core.content.edit

data class WallpaperSettings(
    val blurRadius: Float = 0f,
    val overlayAlpha: Float = 0.12f,
    val gradientAlpha: Float = 0.35f,
    val alignmentX: Float = 0f,
    val alignmentY: Float = 0f,
    val brightness: Float = 100f,
    val contrast: Float = 100f,
    val fitContain: Boolean = false,
)

object BackgroundManager {
    private const val PREFS_NAME = "parcel_prefs"
    private const val KEY_HOME_WALLPAPER_URI = "home_wallpaper_uri"
    private const val KEY_WP_ENABLED = "wp_enabled"
    private const val KEY_WP_BLUR = "wp_blur"
    private const val KEY_WP_OVERLAY = "wp_overlay"
    private const val KEY_WP_GRADIENT = "wp_gradient"
    private const val KEY_WP_ALIGN_X = "wp_align_x"
    private const val KEY_WP_ALIGN_Y = "wp_align_y"
    private const val KEY_WP_BRIGHTNESS = "wp_brightness"
    private const val KEY_WP_CONTRAST = "wp_contrast"
    private const val KEY_WP_FIT = "wp_fit_contain"

    fun getHomeWallpaperUri(context: Context): String {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_HOME_WALLPAPER_URI, "") ?: ""
        } catch (_: Exception) { "" }
    }

    fun setHomeWallpaperUri(context: Context, uri: String) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_HOME_WALLPAPER_URI, uri)
                    putBoolean(KEY_WP_ENABLED, true)
                }
        } catch (_: Exception) { }
    }

    /** 主页壁纸开关（R6 回迁，默认关闭）。 */
    fun isWallpaperEnabled(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_WP_ENABLED, false)
        } catch (_: Exception) { false }
    }

    fun setWallpaperEnabled(context: Context, enabled: Boolean) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_WP_ENABLED, enabled) }
        } catch (_: Exception) { }
    }

    fun getWallpaperSettings(context: Context): WallpaperSettings {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            WallpaperSettings(
                blurRadius = prefs.getFloat(KEY_WP_BLUR, 0f).coerceIn(0f, 40f),
                overlayAlpha = prefs.getFloat(KEY_WP_OVERLAY, 0.12f).coerceIn(0f, 0.5f),
                gradientAlpha = prefs.getFloat(KEY_WP_GRADIENT, 0.35f).coerceIn(0.1f, 0.8f),
                alignmentX = prefs.getFloat(KEY_WP_ALIGN_X, 0f).coerceIn(-1f, 1f),
                alignmentY = prefs.getFloat(KEY_WP_ALIGN_Y, 0f).coerceIn(-1f, 1f),
                brightness = prefs.getFloat(KEY_WP_BRIGHTNESS, 100f).coerceIn(50f, 150f),
                contrast = prefs.getFloat(KEY_WP_CONTRAST, 100f).coerceIn(50f, 150f),
                fitContain = prefs.getBoolean(KEY_WP_FIT, false),
            )
        } catch (_: Exception) { WallpaperSettings() }
    }

    fun setWallpaperSettings(context: Context, settings: WallpaperSettings) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putFloat(KEY_WP_BLUR, settings.blurRadius)
                putFloat(KEY_WP_OVERLAY, settings.overlayAlpha)
                putFloat(KEY_WP_GRADIENT, settings.gradientAlpha)
                putFloat(KEY_WP_ALIGN_X, settings.alignmentX)
                putFloat(KEY_WP_ALIGN_Y, settings.alignmentY)
                putFloat(KEY_WP_BRIGHTNESS, settings.brightness.coerceIn(50f, 150f))
                putFloat(KEY_WP_CONTRAST, settings.contrast.coerceIn(50f, 150f))
                putBoolean(KEY_WP_FIT, settings.fitContain)
            }
        } catch (_: Exception) { }
    }

    fun clearHomeWallpaper(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                remove(KEY_HOME_WALLPAPER_URI)
                putBoolean(KEY_WP_ENABLED, false)
            }
        } catch (_: Exception) { }
    }
}
