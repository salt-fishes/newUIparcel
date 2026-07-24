package com.chenran.parcel.util

import android.content.Context
import androidx.core.content.edit

object SeniorModeManager {
    private const val PREFS_NAME = "parcel_prefs"
    private const val KEY_SENIOR_MODE = "senior_mode"

    fun getSeniorMode(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SENIOR_MODE, false)
        } catch (_: Exception) {
            false
        }
    }

    fun saveSeniorMode(context: Context, isSenior: Boolean) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_SENIOR_MODE, isSenior) }
        } catch (_: Exception) {
        }
    }
}
