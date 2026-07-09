package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * DebitCardDao manages the persistence of student payment methods.
 */
@Dao
interface DebitCardDao
{
    /**
     * Persists or replaces card details.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: DebitCardEntity)

    /**
     * Retrieves all saved cards for a specific user ID.
     */
    @Query("SELECT * FROM debit_cards WHERE userId = :userId")
    fun getCardsByUserId(userId: String): Flow<List<DebitCardEntity>>

    /**
     * Deletes a card record from the database.
     */
    @Delete
    suspend fun deleteCard(card: DebitCardEntity)
}
