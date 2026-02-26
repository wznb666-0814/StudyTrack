package com.repea.studytrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.Subject

@Database(entities = [Subject::class, ExamRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun examRecordDao(): ExamRecordDao
}
