package com.repea.studytrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "semesters",
    indices = [
        Index(value = ["name", "userId"], unique = true)
    ]
)
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val userId: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
