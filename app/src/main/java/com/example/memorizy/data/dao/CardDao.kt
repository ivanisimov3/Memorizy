package com.example.memorizy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memorizy.data.entity.Card
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM cards WHERE setId = :setId ORDER BY id ASC")
    fun getAllCardsForSet(setId: Int): Flow<List<Card>>
}