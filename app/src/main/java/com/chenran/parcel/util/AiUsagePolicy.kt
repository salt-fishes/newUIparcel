package com.chenran.parcel.util

import android.content.Context
import com.chenran.parcel.util.db.ParcelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AiUsagePolicy {
    fun tryConsume(context: Context, dailyLimit: Int): Boolean = runBlocking(Dispatchers.IO) {
        ParcelDatabase.get(context).aiUsageDao().tryConsume(dayKey(), dailyLimit)
    }

    fun requestCount(context: Context): Int = runBlocking(Dispatchers.IO) {
        ParcelDatabase.get(context).aiUsageDao().count(dayKey()) ?: 0
    }

    private fun dayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
}
