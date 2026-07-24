package com.chenran.parcel.ui.home

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "parcel_prefs"
private const val KEY_SHOW_COMPLETED = "show_completed_codes"
private const val KEY_SHOW_CODE_TIME = "show_code_time"
private const val KEY_HORIZONTAL_LAYOUT = "horizontal_layout"

fun saveShowCompleted(context: Context, show: Boolean) {
    try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_SHOW_COMPLETED, show) }
    } catch (_: Exception) {
    }
}

fun getShowCompleted(context: Context): Boolean {
    return try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_COMPLETED, true)
    } catch (_: Exception) {
        true
    }
}

fun saveShowCodeTime(context: Context, show: Boolean) {
    try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_SHOW_CODE_TIME, show) }
    } catch (_: Exception) {
    }
}

fun getShowCodeTime(context: Context): Boolean {
    return try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_CODE_TIME, true)
    } catch (_: Exception) {
        true
    }
}

fun saveHorizontalLayout(context: Context, horizontal: Boolean) {
    try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_HORIZONTAL_LAYOUT, horizontal) }
    } catch (_: Exception) {
    }
}

fun getHorizontalLayout(context: Context): Boolean {
    return try {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HORIZONTAL_LAYOUT, false)
    } catch (_: Exception) {
        false
    }
}
