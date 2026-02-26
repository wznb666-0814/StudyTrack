package com.repea.studytrack.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamType
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.repository.StudyRepository
import com.repea.studytrack.utils.ExcelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val excelHelper: ExcelHelper,
    private val repository: StudyRepository
) : ViewModel() {

    private val _exportStatus = MutableSharedFlow<String>()
    val exportStatus = _exportStatus.asSharedFlow()

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val subjects = repository.getAllSubjects().first()
                val records = repository.getAllRecords().first()
                val success = excelHelper.exportToUri(uri, subjects, records)
                if (success) {
                    _exportStatus.emit("导出成功: $uri")
                } else {
                    _exportStatus.emit("导出失败")
                }
            } catch (e: Exception) {
                _exportStatus.emit("导出出错: ${e.message}")
            }
        }
    }

    // Keep old one for backup if needed, but UI will use new one
    fun backupData() {
        viewModelScope.launch {
            try {
                val subjects = repository.getAllSubjects().first()
                val records = repository.getAllRecords().first()
                val uri = excelHelper.exportToExcel(subjects, records)
                if (uri != null) {
                    _exportStatus.emit("备份成功: $uri")
                } else {
                    _exportStatus.emit("备份失败")
                }
            } catch (e: Exception) {
                _exportStatus.emit("备份出错: ${e.message}")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            val parsedRecords = excelHelper.readExcel(uri)
            parsedRecords.forEach { record ->
                // Find or create subject
                var subjectId = repository.getSubjectByName(record.subjectName)?.id
                if (subjectId == null) {
                    val newSubject = Subject(name = record.subjectName, fullScore = 100.0)
                    subjectId = repository.addSubject(newSubject).toInt()
                }

                repository.addRecord(
                    ExamRecord(
                        subjectId = subjectId,
                        examName = record.examName,
                        examDate = record.date,
                        score = record.score,
                        examType = ExamType.fromLabel(record.type).label,
                        classRank = record.classRank,
                        gradeRank = record.gradeRank,
                        districtRank = record.districtRank,
                        reflection = record.reflection,
                        imageUri = null
                    )
                )
            }
            _exportStatus.emit("导入完成")
        }
    }
}
