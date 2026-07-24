package com.chenran.parcel.util.ai

/** 全局限速，避免多条新消息同时触发 AI 请求造成限流或发热。 */
object AiRequestThrottle {
    private const val MIN_INTERVAL_MS = 900L
    private val lock = Any()
    private var nextAllowedAt = 0L

    fun awaitTurn() {
        val waitMs = synchronized(lock) {
            val now = System.currentTimeMillis()
            val wait = (nextAllowedAt - now).coerceAtLeast(0L)
            nextAllowedAt = maxOf(now, nextAllowedAt) + MIN_INTERVAL_MS
            wait
        }
        if (waitMs > 0L) {
            try {
                Thread.sleep(waitMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
