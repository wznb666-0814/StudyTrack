package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val prefs = prefsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.repea.studytrack.repository.UserPreferencesState())

    init {
        viewModelScope.launch {
            ensureDefaultUser()
        }
    }

    private suspend fun ensureDefaultUser() {
        val currentUsers = repository.getAllUsers().first()

        // 初次启动：没有任何用户时创建一个默认用户，并设置为当前用户
        if (currentUsers.isEmpty()) {
            val newId = repository.addUser(UserProfile(name = "默认用户"))
            prefsRepository.setCurrentUserId(newId.toInt())
            return
        }

        // 不再自动删除名称相同的用户，也不随意修改已有的 currentUserId，
        // 只在当前 ID 已无效（例如数据迁移异常）时，兜底切到第一个用户。
        val prefs = prefsRepository.preferences.first()
        if (currentUsers.none { it.id == prefs.currentUserId }) {
            prefsRepository.setCurrentUserId(currentUsers.first().id)
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
        }
    }

    fun addUser(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val newId = repository.addUser(UserProfile(name = trimmed))
            // 如果当前没有有效用户，则切换到新用户
            val currentPrefs = prefsRepository.preferences.first()
            if (users.value.isEmpty() || users.value.none { it.id == currentPrefs.currentUserId }) {
                prefsRepository.setCurrentUserId(newId.toInt())
            }
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
                }
            }
        }
    }
}

