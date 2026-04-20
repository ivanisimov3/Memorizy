package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

// Запросы в БД к записям учебных сессий

@Dao
interface SessionRecordDao {

    /*
    Сохранить результат сессии локально
    */
    @Insert
    suspend fun insertRecord(record: SessionRecord)

    /*
    Получить все сессии для определённого набора, отсортированные по времени,
    Чтобы построить линейный график прогресса
    */
    @Query("SELECT * FROM session_records WHERE setId = :setId ORDER BY timestamp ASC")
    fun getRecordsForSet(setId: Long): Flow<List<SessionRecord>>
}