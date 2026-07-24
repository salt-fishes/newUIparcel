package com.chenran.parcel.util.ai

import android.content.Context
import com.chenran.parcel.model.SmsModel
import com.chenran.parcel.util.SmsParser

data class ResolvedParse(
    val address: String,
    val code: String,
    val lockerNumber: String,
    val success: Boolean,
    val isPickupSms: Boolean,
    val aiResult: AiParseResult? = null,
)

object SmartParseResolver {
    fun resolve(
        context: Context?,
        sms: SmsModel,
        parser: SmsParser,
        config: AiConfig?,
    ): ResolvedParse {
        val regex = parser.parseSms(sms.body)
        if (context == null || config == null || !config.enabled || config.parseStrategy == ParseStrategy.REGEX_ONLY) {
            return regex.toResolved()
        }

        val record = ParseLedger.get(context, ParseLedger.fingerprint(sms))
        val ai = record?.takeIf { it.isReusable() }?.result
        if (ai != null) {
            return ResolvedParse(
                address = ai.address,
                code = ai.code,
                lockerNumber = ai.lockerNumber,
                success = ai.success,
                isPickupSms = ai.isPickupSms,
                aiResult = ai,
            )
        }

        return when (config.parseStrategy) {
            ParseStrategy.REGEX_ONLY -> regex.toResolved()
            ParseStrategy.AI_PREFERRED -> if (config.fallbackToRegex) regex.toResolved() else pendingResult(regex.isPickupSms)
            ParseStrategy.AI_ONLY -> pendingResult(regex.isPickupSms)
        }
    }

    private fun SmsParser.ParseResult.toResolved() = ResolvedParse(
        address = address,
        code = code,
        lockerNumber = lockerNumber,
        success = success,
        isPickupSms = isPickupSms,
    )

    private fun pendingResult(isPickupHint: Boolean) = ResolvedParse(
        address = "",
        code = "",
        lockerNumber = "",
        success = false,
        isPickupSms = isPickupHint,
    )
}
