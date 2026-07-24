package com.chenran.parcel.util.ai

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.chenran.parcel.model.SmsModel
import com.chenran.parcel.worker.AiParseWorker

object AiParseScheduler {
    fun enqueueNewMessages(context: Context, messages: List<SmsModel>, config: AiConfig) {
        if (!config.enabled || !config.autoParseNewMessages || config.parseStrategy == ParseStrategy.REGEX_ONLY) return
        val since = config.autoParseSince
        if (since <= 0L) return

        val candidates = messages.filter { it.timestamp >= since }
        if (candidates.isEmpty()) return

        // 一次性读取账本做批量过滤，避免每条消息一次查询
        val now = System.currentTimeMillis()
        val ledger = ParseLedger.getAll(context)
        val toEnqueue = candidates.filter { sms ->
            val existing = ledger[ParseLedger.fingerprint(sms)]
            !(existing?.isReusable() == true || existing?.status == ParseStatus.PROCESSING ||
                (existing?.status == ParseStatus.RETRY_WAIT && existing.nextRetryAt > now) ||
                existing?.status == ParseStatus.FAILED)
        }
        if (toEnqueue.isEmpty()) return

        // 批量标记 PENDING（单事务插入 + 单次修剪），再逐条入队 Worker
        ParseLedger.markPendingBatch(context, toEnqueue)
        toEnqueue.forEachIndexed { index, sms ->
            enqueueWork(context, sms, config, manual = false, initialDelayMs = (index % 5) * 250L)
        }
    }

    fun enqueue(context: Context, sms: SmsModel, config: AiConfig) {
        if (!config.enabled || !config.autoParseNewMessages || config.parseStrategy == ParseStrategy.REGEX_ONLY) return
        if (config.autoParseSince <= 0L || sms.timestamp < config.autoParseSince) return
        enqueueInternal(context, sms, config, manual = false)
    }

    fun retryNow(context: Context, sms: SmsModel, config: AiConfig): Boolean {
        if (!config.enabled || config.parseStrategy == ParseStrategy.REGEX_ONLY || !config.isValidHttpsConfig()) {
            return false
        }
        ParseLedger.reset(context, ParseLedger.fingerprint(sms))
        enqueueInternal(context, sms, config, manual = true)
        return true
    }

    private fun enqueueInternal(
        context: Context,
        sms: SmsModel,
        config: AiConfig,
        manual: Boolean,
        initialDelayMs: Long = 0L,
    ) {
        val fingerprint = ParseLedger.fingerprint(sms)
        val existing = ParseLedger.get(context, fingerprint)
        val now = System.currentTimeMillis()
        if (!manual) {
            if (existing?.isReusable() == true || existing?.status == ParseStatus.PROCESSING) return
            if (existing?.status == ParseStatus.RETRY_WAIT && existing.nextRetryAt > now) return
            if (existing?.status == ParseStatus.FAILED) return
        }
        ParseLedger.markPending(context, sms)
        enqueueWork(context, sms, config, manual, initialDelayMs)
    }

    private fun enqueueWork(
        context: Context,
        sms: SmsModel,
        config: AiConfig,
        manual: Boolean,
        initialDelayMs: Long = 0L,
    ) {
        val fingerprint = ParseLedger.fingerprint(sms)
        val input = Data.Builder()
            .putString(AiParseWorker.KEY_FINGERPRINT, fingerprint)
            .putString(AiParseWorker.KEY_MESSAGE_ID, sms.id)
            .putString(AiParseWorker.KEY_BODY, sms.body.take(8_000))
            .putString(AiParseWorker.KEY_SOURCE, sms.source)
            .putLong(AiParseWorker.KEY_TIMESTAMP, sms.timestamp)
            .putBoolean(AiParseWorker.KEY_MANUAL_RETRY, manual)
            .build()
        val networkType = if (config.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val requestBuilder = OneTimeWorkRequestBuilder<AiParseWorker>()
            .setInputData(input)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        if (initialDelayMs > 0L) {
            requestBuilder.setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
        }
        val request = requestBuilder.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "ai_parse_$fingerprint",
            if (manual) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
