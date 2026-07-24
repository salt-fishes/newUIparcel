package com.chenran.parcel.util.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

object AiClient {
    private const val MODEL_TIMEOUT_MS = 8_000
    private const val PARSE_TIMEOUT_MS = 10_000
    private const val TEST_TIMEOUT_MS = 12_000
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchAvailableModels(config: AiConfig): Result<List<String>> = runCatching {
        requireHttps(config.apiUrl)
        require(config.apiKey.isNotBlank()) { "请先填写 API Key 密钥" }
        val endpoint = modelsEndpoint(config.apiUrl)
        val response = request("GET", endpoint, config.apiKey, null, MODEL_TIMEOUT_MS)
        if (response.code !in 200..299) error(httpMessage(response.code, response.body))
        val root = json.parseToJsonElement(response.body).jsonObject
        root["data"]?.jsonArray?.mapNotNull { item ->
            item.jsonObject["id"]?.jsonPrimitive?.contentOrNull
                ?: item.jsonObject["name"]?.jsonPrimitive?.contentOrNull
        }?.filter { it.isNotBlank() }?.distinct().orEmpty()
    }

    fun testConnectivity(config: AiConfig): AiConnectivityResult {
        val started = System.nanoTime()
        return try {
            requireHttps(config.apiUrl)
            require(config.apiKey.isNotBlank()) { "请先填写 API Key 密钥" }
            val body = chatBody(config, "测试API连通性：菜鸟驿站包裹已到测试点，取件码 1-2-3456。", 100)
            val response = request("POST", normalizeChatEndpoint(config.apiUrl), config.apiKey, body, TEST_TIMEOUT_MS)
            val latency = (System.nanoTime() - started) / 1_000_000
            if (response.code !in 200..299) AiConnectivityResult(false, httpMessage(response.code, response.body), latency)
            else AiConnectivityResult(true, "连通测试成功，响应延迟 ${latency}ms", latency, response.body.take(160))
        } catch (e: Exception) {
            val message = if (e is java.net.SocketTimeoutException) "连接超时（超过 12 秒）" else e.message ?: "无法连接到指定 API"
            AiConnectivityResult(false, message)
        }
    }

    fun parseSms(config: AiConfig, smsText: String): Result<AiParseResult> = runCatching {
        require(config.enabled) { "AI 解析未启用" }
        requireHttps(config.apiUrl)
        require(config.apiKey.isNotBlank()) { "请先填写 API Key 密钥" }
        val response = request("POST", normalizeChatEndpoint(config.apiUrl), config.apiKey, chatBody(config, "短信原文：\n$smsText", null), PARSE_TIMEOUT_MS)
        if (response.code !in 200..299) error(httpMessage(response.code, response.body))
        val root = json.parseToJsonElement(response.body).jsonObject
        val content = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull.orEmpty()
        val objectText = Regex("\\{[\\s\\S]*\\}").find(content)?.value ?: content.trim().removePrefix("```").removeSuffix("```").trim()
        val parsed = json.parseToJsonElement(objectText).jsonObject
        AiParseResult(
            isPickupSms = parsed.stringBoolean("isPickupSms"),
            address = parsed.stringValue("address"),
            code = parsed.stringValue("code"),
            lockerNumber = parsed.stringValue("lockerNumber"),
            canonicalAddress = parsed.stringValue("canonicalAddress"),
            suggestedGroup = parsed.stringValue("suggestedGroup"),
            confidence = parsed.doubleValue("confidence").coerceIn(0.0, 1.0),
        )
    }

    private fun JsonObject.stringValue(key: String): String = this[key]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    private fun JsonObject.stringBoolean(key: String): Boolean = this[key]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false
    private fun JsonObject.doubleValue(key: String): Double = this[key]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0

    private fun chatBody(config: AiConfig, userText: String, maxTokens: Int?): String = buildJsonObject {
        put("model", config.modelName.ifBlank { "deepseek-chat" })
        put("temperature", config.temperature)
        put("messages", kotlinx.serialization.json.buildJsonArray {
            add(buildJsonObject { put("role", "system"); put("content", config.systemPrompt.ifBlank { DEFAULT_EXPRESS_SYSTEM_PROMPT }) })
            add(buildJsonObject { put("role", "user"); put("content", userText) })
        })
        if (maxTokens != null) put("max_tokens", maxTokens)
    }.toString()

    private data class Response(val code: Int, val body: String)

    private fun request(method: String, endpoint: String, apiKey: String, body: String?, timeoutMs: Int): Response {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            return Response(connection.responseCode, text)
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeChatEndpoint(raw: String): String {
        val value = raw.trim().trimEnd('/')
        return if (value.endsWith("/chat/completions")) value else if (value.endsWith("/v1")) "$value/chat/completions" else "$value/v1/chat/completions"
    }

    private fun modelsEndpoint(raw: String): String = normalizeChatEndpoint(raw).removeSuffix("/chat/completions") + "/models"

    private fun requireHttps(raw: String) {
        require(raw.trim().startsWith("https://", ignoreCase = true)) { "仅支持 HTTPS API 地址" }
    }

    private fun httpMessage(code: Int, body: String): String {
        val detail = runCatching { json.parseToJsonElement(body).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull }.getOrNull()
        return when (code) {
            401 -> "身份验证失败 [HTTP 401]：API Key 无效"
            404 -> "接口或模型未找到 [HTTP 404]"
            else -> "接口返回错误 [HTTP $code]${detail?.let { "：$it" } ?: ""}"
        }
    }
}
