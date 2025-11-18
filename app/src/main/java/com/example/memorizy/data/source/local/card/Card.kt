package com.example.memorizy.data.source.local.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.memorizy.data.source.local.studyset.StudySet

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = StudySet::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("setId"),
        onDelete = ForeignKey.Companion.CASCADE  // удаляем все карточки если удалится набор
    )],
    indices = [Index(value = ["setId"])]    // ускоряем операцию SELECT по столбцу setId
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val setId: Int, // внешний ключ
    val term: String,
    val definition: String
)