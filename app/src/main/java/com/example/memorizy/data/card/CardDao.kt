package com.example.memorizy.data.card

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM cards WHERE setId = :setId ORDER BY id ASC")
    fun getAllCards(setId: Int): Flow<List<Card>>
}