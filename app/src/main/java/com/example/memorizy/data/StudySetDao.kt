package com.example.memorizy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data Access Object - определяет методы, с помощью которых происходит взаимодействие с данными
@Dao
interface StudySetDao {

    // При конфликте (например если одинаковые id наборов) использовать первый добавленный
    @Insert(onConflict = IGNORE)
    suspend fun insertSet(studySet: StudySet)

    @Delete
    suspend fun deleteSet(studySet: StudySet)

    @Query ("SELECT * FROM study_sets WHERE id = :setId")
    fun getSet(setId: Int): Flow<StudySet>

    // считаем сколько карточек принадлежит конкретному набору с помощью соотнесения id и setId
    // и возвращаем все наборы с подчитанным количеством карточек
    @Query("""
        SELECT
            study_sets.*,
            COUNT(cards.id) as cards_count
        FROM
            study_sets
        LEFT JOIN
            cards ON study_sets.id = cards.setId
        GROUP BY
            study_sets.id
    """)
    fun getAllSetsWithCardCount(): Flow<List<StudySetWithCardCount>>
}