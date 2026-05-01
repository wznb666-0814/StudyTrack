package com.repea.studytrack.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.repea.studytrack.data.local.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeStyle {
    PURE_WHITE,
    LIQUID_GLASS
}

data class UserPreferencesState(
    val themeStyle: AppThemeStyle = AppThemeStyle.PURE_WHITE,
    val customWallpaperUri: String? = null,
    val glassBlurRadiusDp: Float = 0f,
    val glassRefractionHeightDp: Float = 15f,
    val glassRefractionAmountDp: Float = 20f,
    val glassTintAlpha: Float = 0f,
    val glassBorderAlpha: Float = 0.20f,
    val glassVibrancyEnabled: Boolean = true,
    val glassChromaticAberration: Boolean = true,
    val multiUserEnabled: Boolean = false,
    val currentUserId: Int = 1,
    val currentSemesterId: Int = 1
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val THEME_STYLE = stringPreferencesKey("theme_style")
    private val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
    private val GLASS_BLUR_DP = floatPreferencesKey("glass_blur_dp")
    private val GLASS_REFRACTION_HEIGHT_DP = floatPreferencesKey("glass_refraction_height_dp")
    private val GLASS_REFRACTION_AMOUNT_DP = floatPreferencesKey("glass_refraction_amount_dp")
    private val GLASS_TINT_ALPHA = floatPreferencesKey("glass_tint_alpha")
    private val GLASS_BORDER_ALPHA = floatPreferencesKey("glass_border_alpha")
    private val GLASS_VIBRANCY = booleanPreferencesKey("glass_vibrancy")
    private val GLASS_CHROMATIC = booleanPreferencesKey("glass_chromatic")
    private val MULTI_USER_ENABLED = booleanPreferencesKey("multi_user_enabled")
    private val CURRENT_USER_ID = intPreferencesKey("current_user_id")
    private val CURRENT_SEMESTER_ID = intPreferencesKey("current_semester_id")

    val preferences: Flow<UserPreferencesState> = context.dataStore.data.map { p ->
        val themeStyle = runCatching {
            p[THEME_STYLE]?.let { AppThemeStyle.valueOf(it) }
        }.getOrNull() ?: AppThemeStyle.PURE_WHITE
        UserPreferencesState(
            themeStyle = themeStyle,
            customWallpaperUri = p[CUSTOM_WALLPAPER_URI],
            glassBlurRadiusDp = p[GLASS_BLUR_DP] ?: 0f,
            glassRefractionHeightDp = p[GLASS_REFRACTION_HEIGHT_DP] ?: 15f,
            glassRefractionAmountDp = p[GLASS_REFRACTION_AMOUNT_DP] ?: 20f,
            glassTintAlpha = p[GLASS_TINT_ALPHA] ?: 0f,
            glassBorderAlpha = p[GLASS_BORDER_ALPHA] ?: 0.20f,
            glassVibrancyEnabled = p[GLASS_VIBRANCY] ?: true,
            glassChromaticAberration = p[GLASS_CHROMATIC] ?: true,
            multiUserEnabled = p[MULTI_USER_ENABLED] ?: false,
            currentUserId = p[CURRENT_USER_ID] ?: 1,
            currentSemesterId = p[CURRENT_SEMESTER_ID] ?: 1
        )
    }

    suspend fun setThemeStyle(style: AppThemeStyle) {
        context.dataStore.edit { p -> p[THEME_STYLE] = style.name }
    }

    suspend fun setCustomWallpaperUri(uri: String?) {
        context.dataStore.edit { p ->
            if (uri.isNullOrBlank()) {
                p.remove(CUSTOM_WALLPAPER_URI)
            } else {
                p[CUSTOM_WALLPAPER_URI] = uri
            }
        }
    }

    suspend fun setGlassBlurRadiusDp(value: Float) {
        context.dataStore.edit { p -> p[GLASS_BLUR_DP] = value.coerceIn(0f, 48f) }
    }

    suspend fun setGlassRefractionHeightDp(value: Float) {
        context.dataStore.edit { p -> p[GLASS_REFRACTION_HEIGHT_DP] = value.coerceIn(0f, 48f) }
    }

    suspend fun setGlassRefractionAmountDp(value: Float) {
        context.dataStore.edit { p -> p[GLASS_REFRACTION_AMOUNT_DP] = value.coerceIn(0f, 96f) }
    }

    suspend fun setGlassTintAlpha(value: Float) {
        context.dataStore.edit { p -> p[GLASS_TINT_ALPHA] = value.coerceIn(0f, 0.5f) }
    }

    suspend fun setGlassBorderAlpha(value: Float) {
        context.dataStore.edit { p -> p[GLASS_BORDER_ALPHA] = value.coerceIn(0f, 0.6f) }
    }

    suspend fun setGlassVibrancyEnabled(enabled: Boolean) {
        context.dataStore.edit { p -> p[GLASS_VIBRANCY] = enabled }
    }

    suspend fun setGlassChromaticAberration(enabled: Boolean) {
        context.dataStore.edit { p -> p[GLASS_CHROMATIC] = enabled }
    }

    suspend fun setMultiUserEnabled(enabled: Boolean) {
        context.dataStore.edit { p -> p[MULTI_USER_ENABLED] = enabled }
    }

    suspend fun setCurrentUserId(userId: Int) {
        context.dataStore.edit { p -> p[CURRENT_USER_ID] = userId }
    }

    suspend fun setCurrentSemesterId(semesterId: Int) {
        context.dataStore.edit { p -> p[CURRENT_SEMESTER_ID] = semesterId }
    }
}

