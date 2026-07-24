package com.chenran.parcel.util.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_usage")
data class AiUsageEntity(
    @PrimaryKey val dayKey: String,
    val count: Int,
)
