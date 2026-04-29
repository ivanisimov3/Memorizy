package com.example.memorizy.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.SessionRecordDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.data.source.local.room.entity.StudySet

// Конфигурация локальной БД Room

@Database(entities = [StudySet::class, Card::class, SessionRecord::class], version = 5, exportSchema = false)
@TypeConverters(CardDefinitionVariantsConverter::class) // Конвертер на уровне всей базы данных
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySetDao(): StudySetDao
    abstract fun cardDao(): CardDao
    abstract fun sessionRecordDao(): SessionRecordDao
}