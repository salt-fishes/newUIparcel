package com.chenran.parcel.model

import kotlinx.serialization.Serializable
@Serializable
data class SmsModel(
    val id: String,
    val body: String,
    val timestamp: Long,
    val source: String = "sms",
    val sender: String = "",
    val packageName: String = "",
)
