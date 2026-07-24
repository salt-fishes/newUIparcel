package com.chenran.parcel.worker

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chenran.parcel.util.AiUsagePolicy
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.ai.AiClient
import com.chenran.parcel.util.ai.AiRequestThrottle
import com.chenran.parcel.util.ai.ParseEngine
import com.chenran.parcel.util.ai.ParseLedger
import com.chenran.parcel.util.ai.ParseRecord
import com.chenran.parcel.util.ai.ParseStrategy

class AiParseWorker(
    appContext: Context,
    params: WorkerParameters,
) : Worker(appContext, params) {
    override fun doWork(): Result {
        val fingerprint = inputData.getString(KEY_FINGERPRINT).orEmpty()
        val body = inputData.getString(KEY_BODY).orEmpty()
        if (fingerprint.isBlank() || body.isBlank()) return Result.failure()

        val config = getAiConfig(applicationContext)
        val manualRetry = inputData.getBoolean(KEY_MANUAL_RETRY, false)
        if (!config.enabled || config.parseStrategy == ParseStrategy.REGEX_ONLY || (!manualRetry && !config.autoParseNewMessages)) {
            return Result.success()
        }
        val current = ParseLedger.get(applicationContext, fingerprint) ?: ParseRecord(
            fingerprint = fingerprint,
            messageId = inputData.getString(KEY_MESSAGE_ID).orEmpty(),
            messageTimestamp = inputData.getLong(KEY_TIMESTAMP, 0L),
            source = inputData.getString(KEY_SOURCE).orEmpty(),
        )
        if (current.isReusable()) return Result.success()
        if (!AiUsagePolicy.tryConsume(applicationContext, config.dailyRequestLimit)) {
            ParseLedger.markFailed(applicationContext, current, "已达到每日 AI 请求上限", retryable = false)
            notifyChanged()
            return Result.success()
        }

        ParseLedger.markProcessing(applicationContext, current, ParseEngine.AI, config.modelName)
        AiRequestThrottle.awaitTurn()
        val result = AiClient.parseSms(config, body)
        return result.fold(
            onSuccess = { parsed ->
                ParseLedger.markSuccess(
                    context = applicationContext,
                    record = current,
                    engine = ParseEngine.AI,
                    result = parsed,
                    modelName = config.modelName,
                )
                notifyChanged()
                Result.success()
            },
            onFailure = { error ->
                val message = error.message.orEmpty()
                val authFailure = message.contains("401") || message.contains("403") || message.contains("API Key", ignoreCase = true)
                val retryable = !authFailure && runAttemptCount < MAX_AUTO_RETRIES - 1
                ParseLedger.markFailed(
                    applicationContext,
                    current,
                    message.ifBlank { "AI 解析失败" },
                    retryable,
                )
                notifyChanged()
                if (retryable) Result.retry() else Result.failure()
            },
        )
    }

    private fun notifyChanged() {
        applicationContext.sendBroadcast(
            Intent(ACTION_PARSE_UPDATED).setPackage(applicationContext.packageName),
        )
    }

    companion object {
        const val KEY_FINGERPRINT = "fingerprint"
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_BODY = "body"
        const val KEY_SOURCE = "source"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_MANUAL_RETRY = "manual_retry"
        const val ACTION_PARSE_UPDATED = "com.chenran.parcel.AI_PARSE_UPDATED"
        private const val MAX_AUTO_RETRIES = 3
    }
}
