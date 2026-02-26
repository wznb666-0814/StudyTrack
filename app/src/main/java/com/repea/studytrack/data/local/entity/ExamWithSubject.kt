package com.repea.studytrack.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExamWithSubject(
    @Embedded val exam: ExamRecord,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: Subject
)
