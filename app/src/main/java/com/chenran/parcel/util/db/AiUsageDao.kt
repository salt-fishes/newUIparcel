package com.chenran.parcel.util.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AiUsageDao {
    @Query("SELECT count FROM ai_usage WHERE dayKey = :dayKey LIMIT 1")
    fun count(dayKey: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: AiUsageEntity)

    /** 仅当未超限额时计数 +1；返回受影响行数，0 表示已达限额。 */
    @Query("UPDATE ai_usage SET count = count + 1 WHERE dayKey = :dayKey AND (:dailyLimit <= 0 OR count < :dailyLimit)")
    fun incrementIfBelowLimit(dayKey: String, dailyLimit: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(entity: AiUsageEntity): Long

    /**
     * 原子扣减：INSERT OR IGNORE 建行 + 条件 UPDATE 计数，替代"先查后写"，
     * 避免并发 Worker 同时读取旧值而突破每日限额。
     */
    @Transaction
    fun tryConsume(dayKey: String, dailyLimit: Int): Boolean {
        insertIgnore(AiUsageEntity(dayKey, 0))
        return incrementIfBelowLimit(dayKey, dailyLimit) > 0
    }
}
