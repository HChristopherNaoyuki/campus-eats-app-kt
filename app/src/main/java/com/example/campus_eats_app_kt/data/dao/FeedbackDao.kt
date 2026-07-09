package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import kotlinx.coroutines.flow.Flow

/**
 * FeedbackDao defines the data access logic for user-submitted feedback.
 */
@Dao
interface FeedbackDao
{
    /**
     * Persists user feedback.
     */
    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntity)

    /**
     * Returns a chronological stream of all system feedback.
     */
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>
}
