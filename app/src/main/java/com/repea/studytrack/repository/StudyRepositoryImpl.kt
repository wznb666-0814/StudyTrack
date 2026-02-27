package com.repea.studytrack.repository

import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.dao.UserDao
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val examRecordDao: ExamRecordDao,
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : StudyRepository {

    private suspend fun currentUserId(): Int {
        return userPreferencesRepository.preferences.first().currentUserId
    }

    override fun getAllSubjects(): Flow<List<Subject>> =
        combine(userPreferencesRepository.preferences, subjectDao.getAllSubjects()) { prefs, subjects ->
            subjects.filter { it.userId == prefs.currentUserId }
        }

    override suspend fun getSubjectById(id: Int): Subject? = subjectDao.getSubjectById(id)

    override suspend fun getSubjectByName(name: String): Subject? {
        val userId = currentUserId()
        return subjectDao.getSubjectByName(name, userId)
    }

    override suspend fun addSubject(subject: Subject): Long {
        val userId = currentUserId()
        return subjectDao.insertSubject(subject.copy(userId = userId))
    }

    override suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)

    override suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    override fun getAllRecords(): Flow<List<ExamWithSubject>> =
        combine(userPreferencesRepository.preferences, examRecordDao.getAllRecords()) { prefs, records ->
            records.filter { it.exam.userId == prefs.currentUserId }
        }

    override suspend fun addRecord(record: ExamRecord) {
        val userId = currentUserId()
        examRecordDao.insertRecord(record.copy(userId = userId))
    }

    override suspend fun updateRecord(record: ExamRecord) = examRecordDao.updateRecord(record)

    override suspend fun deleteRecord(record: ExamRecord) = examRecordDao.deleteRecord(record)

    override fun getAllUsers(): Flow<List<UserProfile>> = userDao.getAllUsers()

    override suspend fun getUserById(id: Int): UserProfile? = userDao.getUserById(id)

    override suspend fun addUser(user: UserProfile): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserProfile) = userDao.updateUser(user)

    override suspend fun deleteUser(user: UserProfile) = userDao.deleteUser(user)
}
