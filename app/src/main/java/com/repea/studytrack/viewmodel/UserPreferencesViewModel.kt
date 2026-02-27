package com.repea.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.repository.ForegroundMode
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

    fun setWallpaperUri(uri: String?) {
        viewModelScope.launch { repository.setWallpaperUri(uri) }
    }

    fun setForegroundMode(mode: ForegroundMode) {
        viewModelScope.launch { repository.setForegroundMode(mode) }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setLiquidGlassEnabled(enabled) }
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

    fun setGradeA100(value: Float) {
        viewModelScope.launch { repository.setGradeA100(value) }
    }

    fun setGradeB100(value: Float) {
        viewModelScope.launch { repository.setGradeB100(value) }
    }

    fun setGradeC100(value: Float) {
        viewModelScope.launch { repository.setGradeC100(value) }
    }

    fun setGradeA70(value: Float) {
        viewModelScope.launch { repository.setGradeA70(value) }
    }

    fun setGradeB70(value: Float) {
        viewModelScope.launch { repository.setGradeB70(value) }
    }

    fun setGradeC70(value: Float) {
        viewModelScope.launch { repository.setGradeC70(value) }
    }

    fun setGradeA60(value: Float) {
        viewModelScope.launch { repository.setGradeA60(value) }
    }

    fun setGradeB60(value: Float) {
        viewModelScope.launch { repository.setGradeB60(value) }
    }

    fun setGradeC60(value: Float) {
        viewModelScope.launch { repository.setGradeC60(value) }
    }
}

