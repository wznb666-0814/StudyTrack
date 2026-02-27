package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.Subject
import com.repea.studytrack.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val subjects = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, fullScore: Double) {
        viewModelScope.launch {
            repository.addSubject(Subject(name = name, fullScore = fullScore))
        }
    }

    fun updateSubject(subject: Subject, name: String, fullScore: Double) {
        viewModelScope.launch {
            repository.updateSubject(
                subject.copy(
                    name = name,
                    fullScore = fullScore
                )
            )
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}
