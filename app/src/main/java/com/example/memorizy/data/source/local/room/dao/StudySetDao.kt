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

// Data Access Object for the Study_set table.
@Dao
interface StudySetDao {

    // Insert a set in the database and Ignore property to keep the existing rows
    // @param studySet the set to be inserted
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(studySet: StudySet)

    // Update a set in the database
    // @param studySet the set to be updated
    @Update
    suspend fun updateSet(studySet: StudySet)

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
            study_sets.createdAt ASC
    """)
    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>

    // Select all unsynced sets
    @Query("SELECT * FROM study_sets WHERE remoteId IS NULL")
    suspend fun getUnsyncedSets(): List<StudySet>

    // Select all synced sets
    @Query("SELECT * FROM study_sets WHERE remoteId IS NOT NULL")
    suspend fun getSyncedSets(): List<StudySet>

    // Select specific set
    // @param remoteId the remote id to choose
    // LIMIT 1 - stop as soon as you find needed set (optimization)
    @Query("SELECT * FROM study_sets WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getSetByRemoteId(remoteId: Long): StudySet?

    // Select specific set
    // @param id the set id to choose
    @Query("SELECT * FROM study_sets WHERE id = :id")
    suspend fun getSetByIdSimple(id: Long): StudySet?

    // Delete all sets from study_sets
    @Query("DELETE FROM study_sets")
    suspend fun clearAll()
}