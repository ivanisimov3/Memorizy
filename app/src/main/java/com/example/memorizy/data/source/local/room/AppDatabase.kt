package com.example.memorizy.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet

// The Room Database that contains the Cards and Study_set table.
@Database(entities = [StudySet::class, Card::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySetDao(): StudySetDao
    abstract fun cardDao(): CardDao
}