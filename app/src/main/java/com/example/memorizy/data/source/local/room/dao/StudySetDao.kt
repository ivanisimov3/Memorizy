package com.example.memorizy.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import kotlinx.coroutines.flow.Flow

// Data Access Object for the Study_set table.
@Dao
interface StudySetDao {

    // Insert a set in the database and Ignore property to keep the existing rows
    // @param studySet the set to be inserted
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertSet(studySet: StudySet)

    // Delete chosen set from the table
    // @param studySet the set to be deleted
    @Delete
    suspend fun deleteSet(studySet: StudySet)

    // Select specific set
    // @param setId the set id to choose
    @Query("SELECT * FROM study_sets WHERE id = :setId")
    fun getSet(setId: Long): Flow<StudySet>  // Flow для операций с использованием SELECT

    // Study_sets left join cards by setId and group by id
    @Query("""
        SELECT
            study_sets.*,
            COUNT(cards.id) as cardNumber
        FROM
            study_sets
        LEFT JOIN
            cards ON study_sets.id = cards.setId
        GROUP BY
            study_sets.id
        ORDER BY
            study_sets.id ASC
    """)
    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>
}