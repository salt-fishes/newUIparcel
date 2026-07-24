package com.chenran.parcel.util.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ParseRecordEntity::class, AiUsageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ParcelDatabase : RoomDatabase() {
    abstract fun parseRecordDao(): ParseRecordDao
    abstract fun aiUsageDao(): AiUsageDao

    companion object {
        @Volatile
        private var instance: ParcelDatabase? = null

        fun get(context: Context): ParcelDatabase = instance ?: synchronized(this) {
            instance ?: run {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    ParcelDatabase::class.java,
                    "parcel.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                instance = db
                // 旧数据迁移放 Room 后台线程执行，避免主线程首访时同步迁移造成卡顿；
                // 失败会记日志且下次启动自动重试
                db.queryExecutor.execute {
                    LegacyStoreMigrator.migrateIfNeeded(context.applicationContext, db)
                }
                db
            }
        }
    }
}
