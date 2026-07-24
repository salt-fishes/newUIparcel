package com.chenran.parcel.util.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ParseRecordDao {
    @Query("SELECT * FROM parse_records WHERE fingerprint = :fingerprint LIMIT 1")
    fun get(fingerprint: String): ParseRecordEntity?

    @Query("SELECT * FROM parse_records ORDER BY updatedAt DESC")
    fun getAll(): List<ParseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(record: ParseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(records: List<ParseRecordEntity>)

    @Query(
        "DELETE FROM parse_records WHERE fingerprint NOT IN (" +
            "SELECT fingerprint FROM parse_records ORDER BY updatedAt DESC LIMIT :maxRecords" +
            ")",
    )
    fun trimTo(maxRecords: Int)

    @Transaction
    fun upsertAndTrim(record: ParseRecordEntity, maxRecords: Int) {
        upsert(record)
        trimTo(maxRecords)
    }

    @Transaction
    fun upsertAllAndTrim(records: List<ParseRecordEntity>, maxRecords: Int) {
        upsertAll(records)
        trimTo(maxRecords)
    }
}
