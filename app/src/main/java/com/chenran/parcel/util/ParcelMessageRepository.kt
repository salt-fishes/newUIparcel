package com.chenran.parcel.util

import android.content.Context
import com.chenran.parcel.model.SmsModel
import com.chenran.parcel.util.ai.AiParseScheduler
import com.chenran.parcel.util.ai.MessageFingerprint
import com.chenran.parcel.util.ai.ParseLedger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 短信、通知和手动录入的统一读取与跨来源去重入口。 */
object ParcelMessageRepository {
    suspend fun load(context: Context, daysFilter: Int): Pair<List<SmsModel>, List<SmsModel>> {
        val systemMessages = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customMessages = getCustomSmsByTimeFilter(context, daysFilter)
        val deduplicatedCustom = customMessages.filterNot { custom ->
            custom.source == "notification" && systemMessages.any { system ->
                kotlin.math.abs(system.timestamp - custom.timestamp) <= 12 * 60 * 60 * 1000L &&
                    MessageFingerprint.normalizeBody(system.body) == MessageFingerprint.normalizeBody(custom.body)
            }
        }
        val seen = mutableSetOf<String>()
        val uniqueCustom = deduplicatedCustom.filter { seen.add(ParseLedger.fingerprint(it)) }
        return systemMessages to uniqueCustom
    }

    suspend fun ingestCustom(context: Context, sms: SmsModel): Boolean = withContext(Dispatchers.IO) {
        // 同步块保证"去重检查 + 插入"原子性，避免通知并发回调插入重复
        synchronized(this@ParcelMessageRepository) {
            val fingerprint = ParseLedger.fingerprint(sms)
            val existing = getCustomSmsList(context)
            if (existing.any {
                    ParseLedger.fingerprint(it) == fingerprint &&
                        kotlin.math.abs(it.timestamp - sms.timestamp) <= 12 * 60 * 60 * 1000L
                }
            ) return@synchronized false
            addCustomSms(context, sms)
            true
        }.also { inserted ->
            if (inserted) {
                ParseLedger.markPendingAsync(context, sms)
                AiParseScheduler.enqueue(context, sms, getAiConfig(context))
            }
        }
    }
}
