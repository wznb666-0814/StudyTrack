package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.repository.AppThemeStyle
import com.repea.studytrack.repository.UserPreferencesRepository
import com.repea.studytrack.repository.UserPreferencesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferencesState> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferencesState())

    fun setThemeStyle(style: AppThemeStyle) {
        viewModelScope.launch { repository.setThemeStyle(style) }
    }

    fun setCustomWallpaperUri(uri: String?) {
        viewModelScope.launch { repository.setCustomWallpaperUri(uri) }
    }

    fun clearCustomWallpaper() {
        viewModelScope.launch { repository.setCustomWallpaperUri(null) }
    }

    fun setGlassBlurRadiusDp(value: Float) {
        viewModelScope.launch { repository.setGlassBlurRadiusDp(value) }
    }

    fun setGlassRefractionHeightDp(value: Float) {
        viewModelScope.launch { repository.setGlassRefractionHeightDp(value) }
    }

    fun setGlassRefractionAmountDp(value: Float) {
        viewModelScope.launch { repository.setGlassRefractionAmountDp(value) }
    }

    fun setGlassTintAlpha(value: Float) {
        viewModelScope.launch { repository.setGlassTintAlpha(value) }
    }

    fun setGlassBorderAlpha(value: Float) {
        viewModelScope.launch { repository.setGlassBorderAlpha(value) }
    }

    fun setGlassVibrancyEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setGlassVibrancyEnabled(enabled) }
    }

    fun setGlassChromaticAberration(enabled: Boolean) {
        viewModelScope.launch { repository.setGlassChromaticAberration(enabled) }
    }

    fun setCurrentSemesterId(semesterId: Int) {
        viewModelScope.launch { repository.setCurrentSemesterId(semesterId) }
    }
}

