package com.chenran.parcel.util.ai

import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
enum class ParseStrategy {
    REGEX_ONLY,
    AI_PREFERRED,
    AI_ONLY,
}

@Serializable
enum class ParseStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRY_WAIT,
}

@Serializable
enum class ParseEngine {
    REGEX,
    AI,
}

@Serializable
data class ParseRecord(
    val fingerprint: String,
    val messageId: String,
    val messageTimestamp: Long,
    val source: String = "sms",
    val status: ParseStatus = ParseStatus.PENDING,
    val engine: ParseEngine? = null,
    val result: AiParseResult? = null,
    val parserVersion: Int = CURRENT_PARSER_VERSION,
    val modelName: String = "",
    val retryCount: Int = 0,
    val nextRetryAt: Long = 0L,
    val lastError: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun canRetry(now: Long = System.currentTimeMillis()): Boolean =
        status == ParseStatus.FAILED || (status == ParseStatus.RETRY_WAIT && now >= nextRetryAt)

    fun isReusable(): Boolean = status == ParseStatus.SUCCESS && result != null
}

const val CURRENT_PARSER_VERSION = 2

object MessageFingerprint {
    fun create(
        source: String,
        messageId: String,
        timestamp: Long,
        sender: String = "",
        packageName: String = "",
        body: String,
    ): String {
        val normalizedBody = normalizeBody(body)
        val raw = if (normalizedBody.isNotBlank()) {
            normalizedBody
        } else {
            listOf(
                source.trim().lowercase(),
                messageId.trim(),
                timestamp.toString(),
                sender.trim().lowercase(),
                packageName.trim().lowercase(),
            ).joinToString("\u001F")
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    fun normalizeBody(body: String): String = body
        .removePrefix("【自定义取件短信】")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()
}
