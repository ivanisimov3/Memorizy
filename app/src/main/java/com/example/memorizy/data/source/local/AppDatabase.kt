package com.example.memorizy.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memorizy.data.source.local.card.Card
import com.example.memorizy.data.source.local.card.CardDao
import com.example.memorizy.data.source.local.studyset.StudySet
import com.example.memorizy.data.source.local.studyset.StudySetDao


// The Room Database that contains the Cards and Study_set table.
@Database(entities = [StudySet::class, Card::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySetDao(): StudySetDao
    abstract fun cardDao(): CardDao
}