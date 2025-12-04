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

    // Insert a card in the database and Ignore property to keep the existing rows
    // @param card the card to be inserted
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    // Update a card in the database
    // @param card the card to be updated
    @Update
    suspend fun updateCard(card: Card)

    // Delete chosen card from the table
    // @param card the card to be deleted
    @Delete
    suspend fun deleteCard(card: Card)

    // Select all cards from set
    // @param setId the set id to choose cards from
    @Query("SELECT * FROM cards WHERE setId = :setId ORDER BY createdAt ASC")
    fun getAllCardsForSet(setId: Long): Flow<List<Card>>

    // Select all unsynced cards
    @Query("SELECT * FROM cards WHERE remoteId IS NULL")
    suspend fun getUnsyncedCards(): List<Card>

    // Select specific card
    // @param remoteId the remote id to choose
    // LIMIT 1 - stop as soon as you find needed set (optimization)
    @Query("SELECT * FROM cards WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCardByRemoteId(remoteId: Long): Card?
}