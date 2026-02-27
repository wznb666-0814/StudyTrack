package com.repea.studytrack.utils

object GradeCalculator {

    /**
     * 使用用户自定义分值阈值的等级算法。
     *
     * - 先根据满分（100 / 70 / 60）选择对应的 A/B/C 起始分。
     * - 将 A/B/C 三个阈值按从高到低排序，防止用户填写顺序颠倒。
     * - 分段规则：
     *   - score ≥ A → A
     *   - score ≥ B → B
     *   - score ≥ C → C
     *   - 否则 → D
     * - 其它满分的科目，仍按默认比例：A≥85%，B≥75%，C≥60%。
     */
    fun calculateGradeCustom(
        score: Double,
        fullScore: Double,
        prefs: com.repea.studytrack.repository.UserPreferencesState
    ): String {
        if (fullScore <= 0) return "D"

        return when (fullScore.toInt()) {
            100 -> applyThresholds(score, prefs.gradeA100, prefs.gradeB100, prefs.gradeC100)
            70 -> applyThresholds(score, prefs.gradeA70, prefs.gradeB70, prefs.gradeC70)
            60 -> applyThresholds(score, prefs.gradeA60, prefs.gradeB60, prefs.gradeC60)
            else -> {
                val a = fullScore * 0.85
                val b = fullScore * 0.75
                val c = fullScore * 0.60
                applyThresholds(score, a.toFloat(), b.toFloat(), c.toFloat())
            }
        }
    }

    private fun applyThresholds(
        score: Double,
        aRaw: Float,
        bRaw: Float,
        cRaw: Float
    ): String {
        val sorted = listOf(aRaw.toDouble(), bRaw.toDouble(), cRaw.toDouble())
            .sortedDescending()
        val a = sorted.getOrNull(0) ?: 0.0
        val b = sorted.getOrNull(1) ?: 0.0
        val c = sorted.getOrNull(2) ?: 0.0

        return when {
            score >= a -> "A"
            score >= b -> "B"
            score >= c -> "C"
            else -> "D"
        }
    }
}
