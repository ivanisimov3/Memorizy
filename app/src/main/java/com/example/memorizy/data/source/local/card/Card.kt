package com.example.memorizy.data.source.local.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.memorizy.data.source.local.studyset.StudySet

/*
Internal model used to represent a task stored locally in a Room database. This is used inside
the data layer only.
*/
@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = StudySet::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("setId"),
        onDelete = ForeignKey.CASCADE  // удаляем все карточки если удалится набор
    )],
    indices = [Index(value = ["setId"])]    // ускоряем операцию SELECT по столбцу setId
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val setId: Long, // внешний ключ
    val term: String,
    val definition: String
)