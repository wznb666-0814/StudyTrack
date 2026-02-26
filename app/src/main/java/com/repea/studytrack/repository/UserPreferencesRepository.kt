package com.repea.studytrack.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.repea.studytrack.data.local.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ForegroundMode {
    AUTO,
    LIGHT,
    DARK
}

data class UserPreferencesState(
    val wallpaperUri: String? = null,
    val foregroundMode: ForegroundMode = ForegroundMode.LIGHT,
    val liquidGlassEnabled: Boolean = true,
    val glassBlurRadiusDp: Float = 1f,
    val glassRefractionHeightDp: Float = 25f,
    val glassRefractionAmountDp: Float = 30f,
    val glassTintAlpha: Float = 0.10f,
    val glassBorderAlpha: Float = 0.20f,
    val glassVibrancyEnabled: Boolean = true,
    val glassChromaticAberration: Boolean = true
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
    private val FOREGROUND_MODE = stringPreferencesKey("foreground_mode")
    private val LIQUID_GLASS_ENABLED = booleanPreferencesKey("liquid_glass_enabled")

    private val GLASS_BLUR_DP = floatPreferencesKey("glass_blur_dp")
    private val GLASS_REFRACTION_HEIGHT_DP = floatPreferencesKey("glass_refraction_height_dp")
    private val GLASS_REFRACTION_AMOUNT_DP = floatPreferencesKey("glass_refraction_amount_dp")
    private val GLASS_TINT_ALPHA = floatPreferencesKey("glass_tint_alpha")
    private val GLASS_BORDER_ALPHA = floatPreferencesKey("glass_border_alpha")
    private val GLASS_VIBRANCY = booleanPreferencesKey("glass_vibrancy")
    private val GLASS_CHROMATIC = booleanPreferencesKey("glass_chromatic")

    val preferences: Flow<UserPreferencesState> = context.dataStore.data.map { p ->
        val foregroundMode = runCatching {
            p[FOREGROUND_MODE]?.let { ForegroundMode.valueOf(it) }
        }.getOrNull() ?: ForegroundMode.LIGHT
        UserPreferencesState(
            wallpaperUri = p[WALLPAPER_URI],
            foregroundMode = foregroundMode,
            liquidGlassEnabled = p[LIQUID_GLASS_ENABLED] ?: true,
            glassBlurRadiusDp = p[GLASS_BLUR_DP] ?: 1f,
            glassRefractionHeightDp = p[GLASS_REFRACTION_HEIGHT_DP] ?: 25f,
            glassRefractionAmountDp = p[GLASS_REFRACTION_AMOUNT_DP] ?: 30f,
            glassTintAlpha = p[GLASS_TINT_ALPHA] ?: 0.10f,
            glassBorderAlpha = p[GLASS_BORDER_ALPHA] ?: 0.20f,
            glassVibrancyEnabled = p[GLASS_VIBRANCY] ?: true,
            glassChromaticAberration = p[GLASS_CHROMATIC] ?: true
        )
    }

    suspend fun setWallpaperUri(uri: String?) {
        context.dataStore.edit { p ->
            if (uri.isNullOrBlank()) p.remove(WALLPAPER_URI) else p[WALLPAPER_URI] = uri
        }
    }

    suspend fun setForegroundMode(mode: ForegroundMode) {
        context.dataStore.edit { p -> p[FOREGROUND_MODE] = mode.name }
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.dataStore.edit { p -> p[LIQUID_GLASS_ENABLED] = enabled }
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
}

