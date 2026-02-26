package com.repea.studytrack.data.local.entity

enum class ExamType(val label: String) {
    MONTHLY("月考"),
    MIDTERM("期中"),
    FINAL("期末"),
    OTHER("其他");

    companion object {
        fun fromLabel(label: String): ExamType {
            return values().find { it.label == label } ?: OTHER
        }
    }
}
