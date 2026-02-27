package com.repea.studytrack.repository

import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface StudyRepository {
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: Int): Subject?
    suspend fun getSubjectByName(name: String): Subject?
    suspend fun addSubject(subject: Subject): Long
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subject: Subject)

    fun getAllRecords(): Flow<List<ExamWithSubject>>
    suspend fun addRecord(record: ExamRecord)
    suspend fun updateRecord(record: ExamRecord)
    suspend fun deleteRecord(record: ExamRecord)

    fun getAllUsers(): Flow<List<UserProfile>>
    suspend fun getUserById(id: Int): UserProfile?
    suspend fun addUser(user: UserProfile): Long
    suspend fun updateUser(user: UserProfile)
    suspend fun deleteUser(user: UserProfile)
}
