package com.example.memorizy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memorizy.data.dao.CardDao
import com.example.memorizy.data.dao.StudySetDao
import com.example.memorizy.data.entity.Card
import com.example.memorizy.data.entity.StudySet

@Database(entities = [StudySet::class, Card::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySetDao(): StudySetDao
    abstract fun cardDao(): CardDao
}