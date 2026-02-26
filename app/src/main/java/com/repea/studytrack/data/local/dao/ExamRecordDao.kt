package com.repea.studytrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamWithSubject
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamRecordDao {
    @Transaction
    @Query("SELECT * FROM exam_records ORDER BY examDate DESC")
    fun getAllRecords(): Flow<List<ExamWithSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ExamRecord)

    @Update
    suspend fun updateRecord(record: ExamRecord)

    @Delete
    suspend fun deleteRecord(record: ExamRecord)
}
