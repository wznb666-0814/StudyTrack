package com.repea.studytrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.dao.UserDao
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.data.local.entity.UserProfile

@Database(entities = [Subject::class, ExamRecord::class, UserProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun examRecordDao(): ExamRecordDao
    abstract fun userDao(): UserDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建用户表并插入一个默认用户（ID 固定为 1）
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `users` (`id`, `name`, `createdAt`)
                    VALUES (1, '默认用户', strftime('%s','now') * 1000)
                    """.trimIndent()
                )

                // 为已有表增加 userId 列，并默认归属到默认用户
                db.execSQL(
                    "ALTER TABLE `subjects` ADD COLUMN `userId` INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE `exam_records` ADD COLUMN `userId` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}
