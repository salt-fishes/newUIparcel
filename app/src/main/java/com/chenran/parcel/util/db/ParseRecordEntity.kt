package com.chenran.parcel.util.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chenran.parcel.util.ai.AiParseResult
import com.chenran.parcel.util.ai.ParseEngine
import com.chenran.parcel.util.ai.ParseRecord
import com.chenran.parcel.util.ai.ParseStatus
import kotlinx.serialization.json.Json

@Entity(
    tableName = "parse_records",
    indices = [Index("updatedAt"), Index("messageTimestamp"), Index("status")],
)
data class ParseRecordEntity(
    @PrimaryKey val fingerprint: String,
    val messageId: String,
    val messageTimestamp: Long,
    val source: String,
    val status: String,
    val engine: String?,
    val resultJson: String?,
    val parserVersion: Int,
    val modelName: String,
    val retryCount: Int,
    val nextRetryAt: Long,
    val lastError: String,
    val updatedAt: Long,
)

private val parseRecordJson = Json { ignoreUnknownKeys = true }

fun ParseRecord.toEntity(): ParseRecordEntity = ParseRecordEntity(
    fingerprint = fingerprint,
    messageId = messageId,
    messageTimestamp = messageTimestamp,
    source = source,
    status = status.name,
    engine = engine?.name,
    resultJson = result?.let { parseRecordJson.encodeToString(AiParseResult.serializer(), it) },
    parserVersion = parserVersion,
    modelName = modelName,
    retryCount = retryCount,
    nextRetryAt = nextRetryAt,
    lastError = lastError,
    updatedAt = updatedAt,
)

fun ParseRecordEntity.toModel(): ParseRecord = ParseRecord(
    fingerprint = fingerprint,
    messageId = messageId,
    messageTimestamp = messageTimestamp,
    source = source,
    status = runCatching { ParseStatus.valueOf(status) }.getOrDefault(ParseStatus.PENDING),
    engine = engine?.let { runCatching { ParseEngine.valueOf(it) }.getOrNull() },
    result = resultJson?.let {
        runCatching { parseRecordJson.decodeFromString(AiParseResult.serializer(), it) }.getOrNull()
    },
    parserVersion = parserVersion,
    modelName = modelName,
    retryCount = retryCount,
    nextRetryAt = nextRetryAt,
    lastError = lastError,
    updatedAt = updatedAt,
)
