package com.repea.studytrack.utils

object GradeCalculator {

    fun calculateGrade(score: Double, fullScore: Double): String {
        // Handle specific full scores first as requested
        return when (fullScore) {
            100.0 -> calculateGrade100(score)
            70.0 -> calculateGrade70(score)
            60.0 -> calculateGrade60(score)
            else -> calculateGradeGeneric(score, fullScore)
        }
    }

    private fun calculateGrade100(score: Double): String {
        return when {
            score >= 85 -> "A"
            score >= 75 -> "B"
            score >= 60 -> "C"
            else -> "D"
        }
    }

    private fun calculateGrade70(score: Double): String {
        return when {
            score >= 60 -> "A"
            score >= 50 -> "B"
            score >= 42 -> "C"
            else -> "D"
        }
    }

    private fun calculateGrade60(score: Double): String {
        return when {
            score >= 50 -> "A"
            score >= 40 -> "B"
            score >= 36 -> "C"
            else -> "D"
        }
    }

    private fun calculateGradeGeneric(score: Double, fullScore: Double): String {
        val percentage = score / fullScore
        return when {
            percentage >= 0.85 -> "A"
            percentage >= 0.75 -> "B"
            percentage >= 0.60 -> "C" // Passing score (0.6 * fullScore)
            else -> "D"
        }
    }
    
    fun getPassingScore(fullScore: Double): Double {
        return fullScore * 0.6
    }
}
