package com.example.memorizy.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Модель результата учебной сессии Room

@Entity(
    tableName = "session_records",
    foreignKeys = [ForeignKey(
        entity = StudySet::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("setId"),
        onDelete = ForeignKey.CASCADE   // Удаляем все записи сессий если удалится набор
    )],
    indices = [Index(value = ["setId"])]
)
data class SessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val setId: Long,
    val type: String,
    val correctCount: Int,
    val totalCount: Int,
    val percentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)