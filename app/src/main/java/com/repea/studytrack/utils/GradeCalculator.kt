package com.repea.studytrack.utils

object GradeCalculator {

    fun calculateGradeCustom(
        score: Double,
        fullScore: Double,
        prefs: com.repea.studytrack.repository.UserPreferencesState? = null
    ): String {
        if (fullScore <= 0) return "D"
        val ratio = score / fullScore

        return when {
            ratio >= 0.85 -> "A"
            ratio >= 0.75 -> "B"
            ratio >= 0.60 -> "C"
            else -> "D"
        }
    }
}
