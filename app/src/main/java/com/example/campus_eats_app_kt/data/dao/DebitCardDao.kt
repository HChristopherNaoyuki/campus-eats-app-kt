package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebitCardDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: DebitCardEntity)

    @Query("SELECT * FROM debit_cards WHERE userId = :userId")
    fun getCardsByUserId(userId: String): Flow<List<DebitCardEntity>>

    @Delete
    suspend fun deleteCard(card: DebitCardEntity)
}
