package com.chenran.parcel.util.ai

import android.content.Context
import com.chenran.parcel.model.SmsModel
import com.chenran.parcel.util.db.ParcelDatabase
import com.chenran.parcel.util.db.toEntity
import com.chenran.parcel.util.db.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * 解析账本：Room 持久化实现。
 * suspend 版本（*Async）为首选 API；旧同步 API 保留给 Worker、服务等后台线程调用方，
 * 禁止在主线程直接调用同步版本。
 */
object ParseLedger {
    private const val MAX_RECORDS = 1500

    fun fingerprint(sms: SmsModel): String = MessageFingerprint.create(
        source = sms.source,
        messageId = sms.id,
        timestamp = sms.timestamp,
        sender = sms.sender,
        packageName = sms.packageName,
        body = sms.body,
    )

    // region suspend API（主线程安全）
    suspend fun getAsync(context: Context, fingerprint: String): ParseRecord? = withContext(Dispatchers.IO) {
        ParcelDatabase.get(context).parseRecordDao().get(fingerprint)?.toModel()
    }

    suspend fun getAllAsync(context: Context): Map<String, ParseRecord> = withContext(Dispatchers.IO) {
        ParcelDatabase.get(context).parseRecordDao().getAll().associate { entity ->
            entity.fingerprint to entity.toModel()
        }
    }

    suspend fun upsertAsync(context: Context, record: ParseRecord) = withContext(Dispatchers.IO) {
        ParcelDatabase.get(context).parseRecordDao().upsertAndTrim(
            record.copy(updatedAt = System.currentTimeMillis()).toEntity(),
            MAX_RECORDS,
        )
    }

    suspend fun markPendingAsync(context: Context, sms: SmsModel): ParseRecord {
        val fingerprint = fingerprint(sms)
        getAsync(context, fingerprint)?.let { return it }
        val record = ParseRecord(
            fingerprint = fingerprint,
            messageId = sms.id,
            messageTimestamp = sms.timestamp,
            source = sms.source,
        )
        upsertAsync(context, record)
        return record
    }

    /** 批量标记 PENDING：单事务插入 + 单次修剪，供批量入队使用。 */
    suspend fun markPendingBatchAsync(context: Context, messages: List<SmsModel>) = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) return@withContext
        val dao = ParcelDatabase.get(context).parseRecordDao()
        val existingKeys = dao.getAll().mapTo(HashSet()) { it.fingerprint }
        val now = System.currentTimeMillis()
        val entities = messages
            .distinctBy { fingerprint(it) }
            .filter { fingerprint(it) !in existingKeys }
            .map { sms ->
                ParseRecord(
                    fingerprint = fingerprint(sms),
                    messageId = sms.id,
                    messageTimestamp = sms.timestamp,
                    source = sms.source,
                    updatedAt = now,
                ).toEntity()
            }
        if (entities.isNotEmpty()) dao.upsertAllAndTrim(entities, MAX_RECORDS)
    }

    suspend fun markProcessingAsync(context: Context, record: ParseRecord, engine: ParseEngine, modelName: String = "") {
        upsertAsync(
            context,
            record.copy(
                status = ParseStatus.PROCESSING,
                engine = engine,
                modelName = modelName,
                lastError = "",
            ),
        )
    }

    suspend fun markSuccessAsync(
        context: Context,
        record: ParseRecord,
        engine: ParseEngine,
        result: AiParseResult,
        modelName: String = "",
    ) {
        upsertAsync(
            context,
            record.copy(
                status = ParseStatus.SUCCESS,
                engine = engine,
                result = result,
                parserVersion = CURRENT_PARSER_VERSION,
                modelName = modelName,
                nextRetryAt = 0L,
                lastError = "",
            ),
        )
    }

    suspend fun markFailedAsync(context: Context, record: ParseRecord, error: String, retryable: Boolean) {
        val retryCount = record.retryCount + 1
        val delayMs = when (retryCount) {
            1 -> 30_000L
            2 -> 2 * 60_000L
            3 -> 10 * 60_000L
            else -> 60 * 60_000L
        }
        upsertAsync(
            context,
            record.copy(
                status = if (retryable) ParseStatus.RETRY_WAIT else ParseStatus.FAILED,
                retryCount = retryCount,
                nextRetryAt = if (retryable) System.currentTimeMillis() + delayMs else 0L,
                lastError = error.take(240),
            ),
        )
    }

    suspend fun resetAsync(context: Context, fingerprint: String) {
        val current = getAsync(context, fingerprint) ?: return
        upsertAsync(
            context,
            current.copy(
                status = ParseStatus.PENDING,
                retryCount = 0,
                nextRetryAt = 0L,
                lastError = "",
                result = null,
            ),
        )
    }
    // endregion

    // region 旧同步 API（仅限后台线程调用方）
    fun get(context: Context, fingerprint: String): ParseRecord? = runBlocking { getAsync(context, fingerprint) }

    fun getAll(context: Context): Map<String, ParseRecord> = runBlocking { getAllAsync(context) }

    fun upsert(context: Context, record: ParseRecord) = runBlocking { upsertAsync(context, record) }

    fun markPending(context: Context, sms: SmsModel): ParseRecord = runBlocking { markPendingAsync(context, sms) }

    fun markPendingBatch(context: Context, messages: List<SmsModel>) = runBlocking { markPendingBatchAsync(context, messages) }

    fun markProcessing(context: Context, record: ParseRecord, engine: ParseEngine, modelName: String = "") =
        runBlocking { markProcessingAsync(context, record, engine, modelName) }

    fun markSuccess(
        context: Context,
        record: ParseRecord,
        engine: ParseEngine,
        result: AiParseResult,
        modelName: String = "",
    ) = runBlocking { markSuccessAsync(context, record, engine, result, modelName) }

    fun markFailed(context: Context, record: ParseRecord, error: String, retryable: Boolean) =
        runBlocking { markFailedAsync(context, record, error, retryable) }

    fun reset(context: Context, fingerprint: String) = runBlocking { resetAsync(context, fingerprint) }
    // endregion
}
