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

enum class ForegroundMode {
    AUTO,
    LIGHT,
    DARK
}

data class UserPreferencesState(
    val wallpaperUri: String? = null,
    val foregroundMode: ForegroundMode = ForegroundMode.DARK,
    val liquidGlassEnabled: Boolean = true,
    val glassBlurRadiusDp: Float = 10f,
    val glassRefractionHeightDp: Float = 25f,
    val glassRefractionAmountDp: Float = 30f,
    val glassTintAlpha: Float = 0.15f,
    val glassBorderAlpha: Float = 0.20f,
    val glassVibrancyEnabled: Boolean = true,
    val glassChromaticAberration: Boolean = true,
    val multiUserEnabled: Boolean = false,
    val currentUserId: Int = 1,
    // 自定义成绩等级阈值（单位：分）
    val gradeA100: Float = 85f,
    val gradeB100: Float = 75f,
    val gradeC100: Float = 60f,
    val gradeA70: Float = 60f,
    val gradeB70: Float = 50f,
    val gradeC70: Float = 42f,
    val gradeA60: Float = 50f,
    val gradeB60: Float = 40f,
    val gradeC60: Float = 36f
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
    private val MULTI_USER_ENABLED = booleanPreferencesKey("multi_user_enabled")
    private val CURRENT_USER_ID = intPreferencesKey("current_user_id")

    // 自定义等级 DataStore key（沿用旧 key 会自动迁移已有数据时避免冲突，这里用新的命名）
    private val GRADE_A_100 = floatPreferencesKey("grade_a_100")
    private val GRADE_B_100 = floatPreferencesKey("grade_b_100")
    private val GRADE_C_100 = floatPreferencesKey("grade_c_100")
    private val GRADE_A_70 = floatPreferencesKey("grade_a_70")
    private val GRADE_B_70 = floatPreferencesKey("grade_b_70")
    private val GRADE_C_70 = floatPreferencesKey("grade_c_70")
    private val GRADE_A_60 = floatPreferencesKey("grade_a_60")
    private val GRADE_B_60 = floatPreferencesKey("grade_b_60")
    private val GRADE_C_60 = floatPreferencesKey("grade_c_60")

    val preferences: Flow<UserPreferencesState> = context.dataStore.data.map { p ->
        val foregroundMode = runCatching {
            p[FOREGROUND_MODE]?.let { ForegroundMode.valueOf(it) }
        }.getOrNull() ?: ForegroundMode.DARK
        UserPreferencesState(
            wallpaperUri = p[WALLPAPER_URI],
            foregroundMode = foregroundMode,
            liquidGlassEnabled = p[LIQUID_GLASS_ENABLED] ?: true,
            glassBlurRadiusDp = p[GLASS_BLUR_DP] ?: 10f,
            glassRefractionHeightDp = p[GLASS_REFRACTION_HEIGHT_DP] ?: 25f,
            glassRefractionAmountDp = p[GLASS_REFRACTION_AMOUNT_DP] ?: 30f,
            glassTintAlpha = p[GLASS_TINT_ALPHA] ?: 0.15f,
            glassBorderAlpha = p[GLASS_BORDER_ALPHA] ?: 0.20f,
            glassVibrancyEnabled = p[GLASS_VIBRANCY] ?: true,
            glassChromaticAberration = p[GLASS_CHROMATIC] ?: true,
            multiUserEnabled = p[MULTI_USER_ENABLED] ?: false,
            currentUserId = p[CURRENT_USER_ID] ?: 1,
            gradeA100 = p[GRADE_A_100] ?: 85f,
            gradeB100 = p[GRADE_B_100] ?: 75f,
            gradeC100 = p[GRADE_C_100] ?: 60f,
            gradeA70 = p[GRADE_A_70] ?: 60f,
            gradeB70 = p[GRADE_B_70] ?: 50f,
            gradeC70 = p[GRADE_C_70] ?: 42f,
            gradeA60 = p[GRADE_A_60] ?: 50f,
            gradeB60 = p[GRADE_B_60] ?: 40f,
            gradeC60 = p[GRADE_C_60] ?: 36f
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

    // 自定义等级阈值：简单限制在合理区间（0 ~ 100）
    suspend fun setGradeA100(value: Float) {
        context.dataStore.edit { p -> p[GRADE_A_100] = value.coerceIn(0f, 100f) }
    }

    suspend fun setGradeB100(value: Float) {
        context.dataStore.edit { p -> p[GRADE_B_100] = value.coerceIn(0f, 100f) }
    }

    suspend fun setGradeC100(value: Float) {
        context.dataStore.edit { p -> p[GRADE_C_100] = value.coerceIn(0f, 100f) }
    }

    suspend fun setGradeA70(value: Float) {
        context.dataStore.edit { p -> p[GRADE_A_70] = value.coerceIn(0f, 70f) }
    }

    suspend fun setGradeB70(value: Float) {
        context.dataStore.edit { p -> p[GRADE_B_70] = value.coerceIn(0f, 70f) }
    }

    suspend fun setGradeC70(value: Float) {
        context.dataStore.edit { p -> p[GRADE_C_70] = value.coerceIn(0f, 70f) }
    }

    suspend fun setGradeA60(value: Float) {
        context.dataStore.edit { p -> p[GRADE_A_60] = value.coerceIn(0f, 60f) }
    }

    suspend fun setGradeB60(value: Float) {
        context.dataStore.edit { p -> p[GRADE_B_60] = value.coerceIn(0f, 60f) }
    }

    suspend fun setGradeC60(value: Float) {
        context.dataStore.edit { p -> p[GRADE_C_60] = value.coerceIn(0f, 60f) }
    }

    suspend fun setMultiUserEnabled(enabled: Boolean) {
        context.dataStore.edit { p -> p[MULTI_USER_ENABLED] = enabled }
    }

    suspend fun setCurrentUserId(userId: Int) {
        context.dataStore.edit { p -> p[CURRENT_USER_ID] = userId }
    }
}

