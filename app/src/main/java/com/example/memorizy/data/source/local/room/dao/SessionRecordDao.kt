package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

// Запросы в БД к записям учебных сессий

@Dao
interface SessionRecordDao {

    /*
    Сохранить результат сессии локально
    */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SessionRecord)

    /*
    Выбрать все несинхронизированные записи учебных сессий,
    Чтобы попытаться их отправить на сервер
    */
    @Query("SELECT * FROM session_records WHERE remoteId IS NULL")
    suspend fun getUnsyncedRecords(): List<SessionRecord>

    /*
    Найти локально запись сессии с id с сервера,
    Чтобы не добавлять повторно уже полученные данные
    */
    @Query("SELECT * FROM session_records WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getRecordByRemoteId(remoteId: Long): SessionRecord?

    /*
    Обновить запись локально
    Когда синхронизировались в первый раз и надо занести поле remoteId
    */
    @Update
    suspend fun updateRecord(record: SessionRecord)

    /*
    Получить все сессии для определённого набора, отсортированные по времени,
    Чтобы построить линейный график прогресса
    */
    @Query("SELECT * FROM session_records WHERE setId = :setId ORDER BY timestamp ASC")
    fun getRecordsForSet(setId: Long): Flow<List<SessionRecord>>
}