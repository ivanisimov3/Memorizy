package com.example.memorizy.data.source.local.card

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data Access Object for the Cards table.
@Dao
interface CardDao {

    // Insert a card in the database and Ignore property to keep the existing rows
    // @param card the card to be inserted
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: Card)

    // Delete chosen card from the table
    // @param card the card to be deleted
    @Delete
    suspend fun deleteCard(card: Card)

    // Select all cards from set
    // @param setId the set id to choose cards from
    @Query("SELECT * FROM cards WHERE setId = :setId ORDER BY id ASC")
    fun getAllCardsForSet(setId: Long): Flow<List<Card>>
}