package com.repea.studytrack.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repea.studytrack.data.local.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    fun completeOnboarding() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_FIRST_LAUNCH] = false
            }
        }
    }
}
