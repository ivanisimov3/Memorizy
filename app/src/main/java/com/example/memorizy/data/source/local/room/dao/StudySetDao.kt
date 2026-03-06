package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import kotlinx.coroutines.flow.Flow

// Запросы в БД к наборам

@Dao
interface StudySetDao {

    /*
    Добавить набор локально (если уже существует, заменить последним добавленным)
    ИЛИ когда на сервере есть, а в клиенте нет
    */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(studySet: StudySet)

    /*
    Выбрать все несинхронизированные наборы,
    Чтобы попытаться их отправить на сервер
    */
    @Query("SELECT * FROM study_sets WHERE remoteId IS NULL")
    suspend fun getUnsyncedSets(): List<StudySet>

    /*
    Выбрать все синхронизированные наборы
    Чтобы потом их удалить локально (если на сервере их уже нет)
    ИЛИ, чтобы убедиться, что набор существует и на сервере, значит можно начать скачивать карточки
    */
    @Query("SELECT * FROM study_sets WHERE remoteId IS NOT NULL")
    suspend fun getSyncedSets(): List<StudySet>

    /*
    В таблице с карточками посчитать сколько карточек принадлежит каждому набору (неудаленных карточек),
    а также сколько из них готовы к повторению (nextReviewDate <= текущее время),
    выбрать все из таблицы с наборами, присоединить столбцы с подсчитанными карточками к каждому набору,
    отсортировать по времени создания набора,
    Чтобы отобразить все наборы с инфой о карточках на главном экране
    */
    @Query("""
        SELECT
            study_sets.*,
            COUNT(cards.id) as cardNumber
        FROM
            study_sets
        LEFT JOIN
            cards ON study_sets.id = cards.setId AND cards.isDeleted = 0
        WHERE
            study_sets.isDeleted = 0
        GROUP BY
            study_sets.id
        ORDER BY
            study_sets.createdAt ASC
    """)
    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>

    /*
    Выбрать определенный набор,
    Чтобы отобразить всю информацию об этом наборе на экране набора
    */
    @Query("SELECT * FROM study_sets WHERE id = :setId")
    fun getSet(setId: Long): Flow<StudySet>  // Flow для операций с использованием SELECT

    /*
    Выбрать определенный набор,
    Чтобы убедиться, что он синхронизирован,
    тогда можно начать синхронизацию с сервером несинхронизированных карточек
    ИЛИ синхронизировать измененные карточки локально
    */
    @Query("SELECT * FROM study_sets WHERE id = :id")
    suspend fun getSetByIdSimple(id: Long): StudySet?

    /*
    Найти локально набор по id с сервера,
    Чтобы работать с полученными данными
    */
    @Query("SELECT * FROM study_sets WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getSetByRemoteId(remoteId: Long): StudySet?

    /*
    Выбрать все наборы, которые синхронизированы с сервером, не должны быть удалены и изменены,
    Чтобы отправить обновление на сервер
    */
    @Query("SELECT * FROM study_sets WHERE isEdited = 1 AND remoteId IS NOT NULL AND isDeleted = 0")
    suspend fun getEditedSets(): List<StudySet>

    /*
    Выбрать все помеченные для удаления и синхронизированные наборы
    Чтобы потом попытаться их удалить на сервере + локально
    */
    @Query("SELECT * FROM study_sets WHERE isDeleted = 1 AND remoteId IS NOT NULL")
    suspend fun getSetsToDelete(): List<StudySet>

    /*
    Обновить набор локально
    ИЛИ когда синхронизировались в первый раз и надо занести поле remoteId
    ИЛИ отредактировали + синхронизировались, следовательно, надо сделать isEdited = 0
    ИЛИ если данные на сервере у синхронизированного набора прилетели измененные
    */
    @Update
    suspend fun updateSet(studySet: StudySet)

    /*
    Пометить набор как удаленный локально
    Когда пользователь удалил набор у себя на устройстве
    */
    @Query("UPDATE study_sets SET isDeleted = 1 WHERE id = :id")
    suspend fun markAsDeletedSet(id: Long)

    /*
    Удалить набор локально
    ИЛИ когда удалили синхронизированный набор на сервере
    ИЛИ если на сервере такого набора не существует
    */
    @Delete
    suspend fun deleteSet(studySet: StudySet)

    /*
    Удалить все наборы, которые связаны с другим пользователем (синхронизированы)
    Если аутентификацию пройдет другой пользователь
    */
    @Query("DELETE FROM study_sets WHERE remoteId IS NOT NULL")
    suspend fun clearSyncedData()

    /*
    Получить название набора по id
    Для формирования текста уведомления в ReviewReminderWorker
    */
    @Query("SELECT name FROM study_sets WHERE id = :setId LIMIT 1")
    suspend fun getSetName(setId: Long): String?
}