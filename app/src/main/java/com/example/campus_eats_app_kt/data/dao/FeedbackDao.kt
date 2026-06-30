package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao
{
    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntity)

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>
}
