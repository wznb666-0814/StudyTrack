package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.entity.Semester
import com.repea.studytrack.data.local.entity.UserProfile
import com.repea.studytrack.repository.StudyRepository
import com.repea.studytrack.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagerViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val users: StateFlow<List<UserProfile>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semesters: StateFlow<List<Semester>> = repository.getAllSemesters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prefs = prefsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.repea.studytrack.repository.UserPreferencesState())

    init {
        viewModelScope.launch {
            ensureDefaultUser()
        }
    }

    private suspend fun ensureDefaultUser() {
        val currentUsers = repository.getAllUsers().first()

        if (currentUsers.isEmpty()) {
            val newId = repository.addUser(UserProfile(name = "默认用户"))
            prefsRepository.setCurrentUserId(newId.toInt())
            ensureCurrentSemesterAvailable()
            return
        }

        val prefs = prefsRepository.preferences.first()
        if (currentUsers.none { it.id == prefs.currentUserId }) {
            prefsRepository.setCurrentUserId(currentUsers.first().id)
        }
        ensureCurrentSemesterAvailable()
    }

    private suspend fun ensureCurrentSemesterAvailable() {
        val prefs = prefsRepository.preferences.first()
        val semesters = repository.getAllSemesters().first()
        if (semesters.isEmpty()) {
            val newId = repository.addSemester(Semester(name = "默认学期"))
            prefsRepository.setCurrentSemesterId(newId.toInt())
            return
        }
        if (semesters.none { it.id == prefs.currentSemesterId }) {
            prefsRepository.setCurrentSemesterId(semesters.first().id)
        }
    }

    fun setMultiUserEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setMultiUserEnabled(enabled)
        }
    }

    fun setCurrentUser(userId: Int) {
        viewModelScope.launch {
            prefsRepository.setCurrentUserId(userId)
            ensureCurrentSemesterAvailable()
        }
    }

    fun addUser(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val newId = repository.addUser(UserProfile(name = trimmed))
            val currentPrefs = prefsRepository.preferences.first()
            if (users.value.isEmpty() || users.value.none { it.id == currentPrefs.currentUserId }) {
                prefsRepository.setCurrentUserId(newId.toInt())
                ensureCurrentSemesterAvailable()
            }
        }
    }

    fun initializePrimaryUser(name: String, semesterName: String = "默认学期") {
        val trimmed = name.trim()
        val trimmedSemester = semesterName.trim().ifBlank { "默认学期" }
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val currentPrefs = prefsRepository.preferences.first()
            val currentUsers = repository.getAllUsers().first()
            val currentUser = currentUsers.firstOrNull { it.id == currentPrefs.currentUserId }
                ?: currentUsers.firstOrNull()

            if (currentUser == null) {
                val newId = repository.addUser(UserProfile(name = trimmed))
                prefsRepository.setCurrentUserId(newId.toInt())
            } else {
                repository.updateUser(currentUser.copy(name = trimmed))
                prefsRepository.setCurrentUserId(currentUser.id)
            }
            val semestersForCurrentUser = repository.getAllSemesters().first()
            val currentSemester = semestersForCurrentUser.firstOrNull { it.id == currentPrefs.currentSemesterId }
                ?: semestersForCurrentUser.firstOrNull()
            val targetSemester = semestersForCurrentUser.firstOrNull { it.name == trimmedSemester }

            when {
                targetSemester != null -> {
                    prefsRepository.setCurrentSemesterId(targetSemester.id)
                }
                currentSemester != null &&
                    semestersForCurrentUser.size == 1 &&
                    currentSemester.name == "默认学期" -> {
                    repository.updateSemester(currentSemester.copy(name = trimmedSemester))
                    prefsRepository.setCurrentSemesterId(currentSemester.id)
                }
                else -> {
                    val semesterId = repository.addSemester(Semester(name = trimmedSemester)).toInt()
                    prefsRepository.setCurrentSemesterId(semesterId)
                }
            }

            ensureCurrentSemesterAvailable()
        }
    }

    fun renameUser(user: UserProfile, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateUser(user.copy(name = trimmed))
        }
    }

    fun deleteUser(user: UserProfile) {
        viewModelScope.launch {
            val currentUsers = users.value
            // 至少保留一个用户
            if (currentUsers.size <= 1) return@launch

            repository.deleteUser(user)

            val prefs = prefsRepository.preferences.first()
            if (prefs.currentUserId == user.id) {
                val remaining = repository.getAllUsers().first().firstOrNull()
                if (remaining != null) {
                    prefsRepository.setCurrentUserId(remaining.id)
                    ensureCurrentSemesterAvailable()
                }
            }
        }
    }
}

