package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet
import kotlinx.coroutines.flow.Flow

// Data Access Object for the Cards table.
@Dao
interface CardDao {

    /*
    Добавить карточку в Room (если уже существует, заменить последним добавленным)
    Нажали кнопку добавить карточку ИЛИ на сервере есть, а в клиенте нет
    */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    /*
    Выбрать все несинхронизированные карточки
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
    отсортировать по времени создания карточки
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
    Найти локально карточку с id с сервера
    Чтобы либо обновить его данными с сервера
    ИЛИ чтобы добавить полностью карточку (если функция вернет null)
    */
    @Query("SELECT * FROM cards WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCardByRemoteId(remoteId: Long): Card?

    /*
    Выбрать все карточки, которые синхронизированы с сервером, не должны быть удалены и изменены
    Чтобы отправить обновление на сервер
    */
    @Query("SELECT * FROM cards WHERE isEdited = 1 AND remoteId IS NOT NULL AND isDeleted = 0")
    suspend fun getEditedCards(): List<Card>

    /*
    Выбрать (все помеченные для удаления и синхронизированные) карточки
    Чтобы потом попытаться их удалить на сервере + локально
    */
    @Query("SELECT * FROM cards WHERE isDeleted = 1 AND remoteId IS NOT NULL")
    suspend fun getCardsToDelete(): List<Card>

    /*
    Обновить существующую карточку
    Синхронизируем локальные с сервером (добавляем remoteId у несинхронизированных)
    ИЛИ скачиваем обновляем данные в соответствии с данными сервера (если карточка существует)
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
    Удалить карточку из Room
    Если успешно синхронизировались с сервером, удалили запись там, удалили и локально
    ИЛИ если на сервере такой карточки не существует, то удаляем локально (СРЕДИ ТЕХ, КОТОРЫЕ ПОМЕЧЕНЫ КАК СИНХРОНИЗИРОВАННЫЕ)
    */
    @Delete
    suspend fun deleteCard(card: Card)
}