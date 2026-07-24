package com.chenran.parcel.util.ai

import kotlinx.serialization.Serializable

const val DEFAULT_EXPRESS_SYSTEM_PROMPT = """
你是一个专业的快递短信解析助手。请从短信中提取取件信息，并只输出 JSON：
{"isPickupSms":true,"address":"短信中的原始地址","code":"取件码","lockerNumber":"柜号或空字符串","canonicalAddress":"标准化地址","suggestedGroup":"建议归类名称","confidence":0.95}
验证码、广告、充值提示等非取件短信返回 isPickupSms=false，其余字符串字段为空，confidence 为 0。置信度必须在 0 到 1 之间。
"""

@Serializable
data class AiConfig(
    val enabled: Boolean = false,
    val providerId: String = "deepseek",
    val apiUrl: String = "https://api.deepseek.com/v1/chat/completions",
    val apiKey: String = "",
    val modelName: String = "deepseek-chat",
    val systemPrompt: String = DEFAULT_EXPRESS_SYSTEM_PROMPT,
    val savedModels: List<String> = emptyList(),
    val temperature: Double = 0.1,
    val imageApiUrl: String = "",
    val parseStrategy: ParseStrategy = ParseStrategy.AI_PREFERRED,
    val autoParseNewMessages: Boolean = true,
    val autoParseSince: Long = 0L,
    val fallbackToRegex: Boolean = true,
    val wifiOnly: Boolean = false,
    val dailyRequestLimit: Int = 50,
    val autoAddressGrouping: Boolean = true,
    val addressConfidenceThreshold: Double = 0.85,
)

@Serializable
data class AiParseResult(
    val isPickupSms: Boolean = false,
    val address: String = "",
    val code: String = "",
    val lockerNumber: String = "",
    val canonicalAddress: String = "",
    val suggestedGroup: String = "",
    val confidence: Double = 0.0,
) {
    val success: Boolean get() = isPickupSms && address.isNotBlank() && code.isNotBlank()
}

data class AiConnectivityResult(
    val success: Boolean,
    val message: String,
    val latencyMs: Long? = null,
    val responseSnippet: String = "",
)

fun AiConfig.isValidHttpsConfig(): Boolean =
    apiKey.isNotBlank() && apiUrl.trim().startsWith("https://", ignoreCase = true)
