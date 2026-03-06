package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memorizy.data.source.local.room.entity.Card
import kotlinx.coroutines.flow.Flow

// Запросы в БД к карточкам

@Dao
interface CardDao {

    /*
    Добавить карточку локально (если уже существует, заменить последним добавленным)
    ИЛИ когда на сервере есть, а в клиенте нет
    */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    /*
    Выбрать все несинхронизированные карточки,
    Чтобы попытаться их отправить на сервер
    */
    @Query("SELECT * FROM cards WHERE remoteId IS NULL")
    suspend fun getUnsyncedCards(): List<Card>

    /*
    Выбрать все синхронизированные карточки определенного набора локально
    Чтобы потом их удалить локально (если на сервере их уже нет)
    */
    @Query("SELECT * FROM cards WHERE remoteId IS NOT NULL AND setId = :setId")
    suspend fun getSyncedCardsBySet(setId: Long): List<Card>

    /*
    В таблице с карточками выбрать все карточки определенного набора (неудаленные карточки),
    отсортировать по времени создания карточки,
    Чтобы отобразить все карточки на экране определенного набора
    */
    @Query("""
        SELECT 
            * 
        FROM 
            cards 
        WHERE 
            setId = :setId AND isDeleted = 0
        ORDER BY 
            createdAt ASC
    """)
    fun getAllCardsForSet(setId: Long): Flow<List<Card>>

    /*
    Выбрать все неудалённые карточки,
    Чтобы подсчитать карточки к повторению на главном экране
    */
    @Query("SELECT * FROM cards WHERE isDeleted = 0")
    fun getAllNonDeletedCards(): Flow<List<Card>>

    /*
    Выбрать все неудалённые карточки (suspend-версия для Worker'а),
    Чтобы проверить какие карточки готовы к повторению
    */
    @Query("SELECT * FROM cards WHERE isDeleted = 0")
    suspend fun getAllNonDeletedCardsSuspend(): List<Card>

    /*
    Найти локально карточку с id с сервера,
    Чтобы работать с данными карточки
    */
    @Query("SELECT * FROM cards WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCardByRemoteId(remoteId: Long): Card?

    /*
    Выбрать все карточки, которые синхронизированы с сервером, не должны быть удалены и изменены,
    Чтобы отправить обновление на сервер
    */
    @Query("SELECT * FROM cards WHERE isEdited = 1 AND remoteId IS NOT NULL AND isDeleted = 0")
    suspend fun getEditedCards(): List<Card>

    /*
    Выбрать все помеченные для удаления и синхронизированные карточки
    Чтобы потом попытаться их удалить на сервере + локально
    */
    @Query("SELECT * FROM cards WHERE isDeleted = 1 AND remoteId IS NOT NULL")
    suspend fun getCardsToDelete(): List<Card>

    /*
    Обновить карточку локально
    ИЛИ когда синхронизировались в первый раз и надо занести поле remoteId
    ИЛИ отредактировали + синхронизировались, следовательно, надо сделать isEdited = 0
    ИЛИ если данные на сервере у синхронизированной карточки прилетили измененные
    */
    @Update
    suspend fun updateCard(card: Card)

    /*
    Пометить карточку как удаленную локально
    Если пользователь удалил карточку у себя на устройстве
    */
    @Query("UPDATE cards SET isDeleted = 1 WHERE id = :id")
    suspend fun markAsDeletedCard(id: Long)

    /*
    Удалить карточку локально
    ИЛИ когда удалили синхронизированную карточку на сервере
    ИЛИ если на сервере такой карточки не существует
    */
    @Delete
    suspend fun deleteCard(card: Card)
}