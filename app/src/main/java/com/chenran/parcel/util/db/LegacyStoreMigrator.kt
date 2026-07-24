package com.chenran.parcel.util.db

import android.content.Context
import android.util.Log
import com.chenran.parcel.util.ai.ParseRecord
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** 一次性迁移旧 SharedPreferences 账本；成功后写入标记，避免重复迁移。 */
object LegacyStoreMigrator {
    private const val MIGRATION_PREFS = "parcel_db_migrations"
    private const val KEY_PARSE_USAGE_V1 = "parse_usage_v1_done"
    private const val OLD_PARSE_PREFS = "parcel_parse_ledger"
    private const val OLD_RECORDS_KEY = "records_v2"
    private const val OLD_USAGE_PREFS = "parcel_ai_usage"
    private const val MAX_RECORDS = 1500
    private val json = Json { ignoreUnknownKeys = true }

    fun migrateIfNeeded(context: Context, db: ParcelDatabase) {
        val migrationPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (migrationPrefs.getBoolean(KEY_PARSE_USAGE_V1, false)) return
        runCatching {
            migrateParseRecords(context, db)
            migrateAiUsage(context, db)
        }.onSuccess {
            migrationPrefs.edit().putBoolean(KEY_PARSE_USAGE_V1, true).commit()
        }.onFailure {
            // 不置完成标记，下次启动自动重试；记录日志便于排查
            Log.e("LegacyStoreMigrator", "旧数据迁移失败，将在下次启动重试", it)
        }
    }

    private fun migrateParseRecords(context: Context, db: ParcelDatabase) {
        val raw = context.getSharedPreferences(OLD_PARSE_PREFS, Context.MODE_PRIVATE)
            .getString(OLD_RECORDS_KEY, null)
            ?: return
        val records = json.decodeFromString<Map<String, ParseRecord>>(raw)
        records.values.forEach { record -> db.parseRecordDao().upsert(record.toEntity()) }
        db.parseRecordDao().trimTo(MAX_RECORDS)
    }

    private fun migrateAiUsage(context: Context, db: ParcelDatabase) {
        val prefs = context.getSharedPreferences(OLD_USAGE_PREFS, Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            if (key.matches(Regex("\\d{8}")) && value is Int) {
                db.aiUsageDao().upsert(AiUsageEntity(key, value))
            }
        }
    }
}
