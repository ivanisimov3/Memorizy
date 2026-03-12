package com.example.memorizy.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Модель карточек для Room

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = StudySet::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("setId"),
        onDelete = ForeignKey.CASCADE   // Удаляем все карточки если удалится набор
    )],
    indices = [Index(value = ["setId"])]    // Индексируем таблицу по setId для увеличения скорости работы
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val setId: Long,
    val term: String,
    val definition: String,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: Long? = null,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val level: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis()
)
