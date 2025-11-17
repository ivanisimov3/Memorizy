package com.example.memorizy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memorizy.data.card.CardDao
import com.example.memorizy.data.studyset.StudySetDao
import com.example.memorizy.data.card.Card
import com.example.memorizy.data.studyset.StudySet

@Database(entities = [StudySet::class, Card::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySetDao(): StudySetDao
    abstract fun cardDao(): CardDao
}