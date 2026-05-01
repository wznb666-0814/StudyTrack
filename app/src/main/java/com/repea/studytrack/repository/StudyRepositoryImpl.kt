package com.repea.studytrack.repository

import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SemesterDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.dao.UserDao
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Semester
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val examRecordDao: ExamRecordDao,
    private val semesterDao: SemesterDao,
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : StudyRepository {

    private suspend fun currentPrefs(): UserPreferencesState {
        return userPreferencesRepository.preferences.first()
    }

    override fun getAllSubjects(): Flow<List<Subject>> =
        combine(userPreferencesRepository.preferences, subjectDao.getAllSubjects()) { prefs, subjects ->
            subjects.filter {
                it.userId == prefs.currentUserId && it.semesterId == prefs.currentSemesterId
            }
        }

    override suspend fun getSubjectById(id: Int): Subject? = subjectDao.getSubjectById(id)

    override suspend fun getSubjectByName(name: String): Subject? {
        val prefs = currentPrefs()
        return subjectDao.getSubjectByName(name, prefs.currentUserId, prefs.currentSemesterId)
    }

    override suspend fun addSubject(subject: Subject): Long {
        val prefs = currentPrefs()
        return subjectDao.insertSubject(
            subject.copy(
                userId = prefs.currentUserId,
                semesterId = prefs.currentSemesterId
            )
        )
    }

    override suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)

    override suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    override fun getAllRecords(): Flow<List<ExamWithSubject>> =
        combine(userPreferencesRepository.preferences, examRecordDao.getAllRecords()) { prefs, records ->
            records.filter {
                it.exam.userId == prefs.currentUserId && it.exam.semesterId == prefs.currentSemesterId
            }
        }

    override suspend fun hasSameRecord(
        subjectId: Int,
        examName: String,
        examDate: Long,
        score: Double,
        semesterId: Int?
    ): Boolean {
        val prefs = currentPrefs()
        val targetSemesterId = semesterId ?: prefs.currentSemesterId
        return examRecordDao.countSameRecord(
            subjectId = subjectId,
            examName = examName,
            examDate = examDate,
            score = score,
            userId = prefs.currentUserId,
            semesterId = targetSemesterId
        ) > 0
    }

    override suspend fun addRecord(record: ExamRecord) {
        val prefs = currentPrefs()
        examRecordDao.insertRecord(
            record.copy(
                userId = prefs.currentUserId,
                semesterId = prefs.currentSemesterId
            )
        )
    }

    override suspend fun updateRecord(record: ExamRecord) = examRecordDao.updateRecord(record)

    override suspend fun deleteRecord(record: ExamRecord) = examRecordDao.deleteRecord(record)

    override fun getAllSemesters(): Flow<List<Semester>> =
        combine(userPreferencesRepository.preferences, semesterDao.getAllSemesters()) { prefs, semesters ->
            semesters.filter { it.userId == prefs.currentUserId }
        }

    override suspend fun getSemesterById(id: Int): Semester? = semesterDao.getSemesterById(id)

    override suspend fun getSemesterByName(name: String): Semester? {
        val prefs = currentPrefs()
        return semesterDao.getSemesterByName(name, prefs.currentUserId)
    }

    override suspend fun countSemesterSubjects(semesterId: Int): Int {
        val prefs = currentPrefs()
        return subjectDao.countSubjectsInSemester(prefs.currentUserId, semesterId)
    }

    override suspend fun countSemesterRecords(semesterId: Int): Int {
        val prefs = currentPrefs()
        return examRecordDao.countRecordsInSemester(prefs.currentUserId, semesterId)
    }

    override suspend fun addSemester(semester: Semester): Long {
        val prefs = currentPrefs()
        return semesterDao.insertSemester(semester.copy(userId = prefs.currentUserId))
    }

    override suspend fun updateSemester(semester: Semester) = semesterDao.updateSemester(semester)

    override suspend fun deleteSemester(semester: Semester) = semesterDao.deleteSemester(semester)

    override fun getAllUsers(): Flow<List<UserProfile>> = userDao.getAllUsers()

    override suspend fun getUserById(id: Int): UserProfile? = userDao.getUserById(id)

    override suspend fun addUser(user: UserProfile): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserProfile) = userDao.updateUser(user)

    override suspend fun deleteUser(user: UserProfile) = userDao.deleteUser(user)
}
