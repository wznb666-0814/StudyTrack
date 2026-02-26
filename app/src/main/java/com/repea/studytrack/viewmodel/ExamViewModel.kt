package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.ExamRecord
import com.repea.studytrack.data.local.entity.ExamType
import com.repea.studytrack.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val allRecords = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecord(
        subjectId: Int,
        examName: String,
        examDate: Long,
        score: Double,
        examType: ExamType,
        classRank: Int?,
        gradeRank: Int?,
        districtRank: Int?,
        reflection: String?,
        imageUri: String?
    ) {
        viewModelScope.launch {
            repository.addRecord(
                ExamRecord(
                    subjectId = subjectId,
                    examName = examName,
                    examDate = examDate,
                    score = score,
                    examType = examType.label,
                    classRank = classRank,
                    gradeRank = gradeRank,
                    districtRank = districtRank,
                    reflection = reflection,
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateRecord(record: ExamRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }
    
    fun deleteRecord(record: ExamRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }
}
