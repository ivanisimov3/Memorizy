package com.example.memorizy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = StudySet::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("setId"),
        onDelete = CASCADE  // удаляем все карточки если удалится набор
    )],
    indices = [Index(value = ["setId"])]    // ускоряем операцию SELECT по столбцу setId
)
data class Card(
    @PrimaryKey val id: Int,

    val setId: Int, // внешний ключ
    val term: String,
    val definition: String
)
