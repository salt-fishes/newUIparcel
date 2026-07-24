package com.chenran.parcel.util

import com.chenran.parcel.util.ai.AiConfig
import com.chenran.parcel.util.ai.AiParseResult

data class AddressResolution(
    val originalAddress: String,
    val canonicalAddress: String,
    val groupName: String,
    val source: String,
    val confidence: Double,
)

object AddressNormalizer {
    private val removableWords = listOf(
        "您的快递已到",
        "快递已到",
        "包裹已到",
    )

    fun normalize(value: String): String {
        var result = value.trim()
            .replace(Regex("[（(][^）)]*[）)]"), "")
            .replace(Regex("[，,。；;：:]+"), " ")
            .replace(Regex("\\s+"), " ")
        removableWords.forEach { word -> result = result.replace(word, "") }
        return result.trim().ifBlank { value.trim() }
    }

    fun resolve(
        originalAddress: String,
        userMappings: Map<String, String>,
        aiResult: AiParseResult?,
        config: AiConfig,
    ): AddressResolution {
        userMappings[originalAddress]?.takeIf { it.isNotBlank() }?.let { tag ->
            return AddressResolution(originalAddress, normalize(originalAddress), tag, "user", 1.0)
        }

        val normalized = normalize(originalAddress)
        userMappings[normalized]?.takeIf { it.isNotBlank() }?.let { tag ->
            return AddressResolution(originalAddress, normalized, tag, "user", 1.0)
        }

        val aiCanonical = aiResult?.canonicalAddress.orEmpty().trim()
        val aiGroup = aiResult?.suggestedGroup.orEmpty().trim()
        val aiConfidence = aiResult?.confidence ?: 0.0
        if (
            config.autoAddressGrouping &&
            aiConfidence >= config.addressConfidenceThreshold &&
            (aiCanonical.isNotBlank() || aiGroup.isNotBlank())
        ) {
            return AddressResolution(
                originalAddress = originalAddress,
                canonicalAddress = aiCanonical.ifBlank { normalized },
                groupName = aiGroup.ifBlank { aiCanonical.ifBlank { normalized } },
                source = "ai",
                confidence = aiConfidence,
            )
        }

        return AddressResolution(
            originalAddress = originalAddress,
            canonicalAddress = normalized,
            groupName = normalized,
            source = "local",
            confidence = if (normalized == originalAddress) 0.7 else 0.8,
        )
    }
}
