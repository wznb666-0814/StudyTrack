package com.repea.studytrack.repository

import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamWithSubject
import com.repea.studytrack.data.local.entity.Subject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val examRecordDao: ExamRecordDao
) : StudyRepository {
    override fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    override suspend fun getSubjectById(id: Int): Subject? = subjectDao.getSubjectById(id)
    override suspend fun getSubjectByName(name: String): Subject? = subjectDao.getSubjectByName(name)
    override suspend fun addSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    override suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    override suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    override fun getAllRecords(): Flow<List<ExamWithSubject>> = examRecordDao.getAllRecords()
    override suspend fun addRecord(record: ExamRecord) = examRecordDao.insertRecord(record)
    override suspend fun updateRecord(record: ExamRecord) = examRecordDao.updateRecord(record)
    override suspend fun deleteRecord(record: ExamRecord) = examRecordDao.deleteRecord(record)
}
