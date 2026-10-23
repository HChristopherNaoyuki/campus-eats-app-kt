package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import kotlinx.coroutines.flow.Flow

/**
 * FeedbackDao defines the data access logic for user-submitted feedback.
 */
@Dao
interface FeedbackDao
{
    /**
     * Persists user feedback, ignoring conflicts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)

    /**
     * Updates the status of a feedback entry.
     */
    @Query("UPDATE feedback SET status = :status, updatedAt = :updatedAt WHERE feedbackId = :feedbackId")
    suspend fun updateFeedbackStatus(feedbackId: Long, status: FeedbackStatus, updatedAt: String)

    /**
     * Finds a feedback entry by user ID and creation timestamp.
     */
    @Query("SELECT * FROM feedback WHERE userId = :userId AND createdAt = :createdAt LIMIT 1")
    suspend fun findByUserAndCreatedAt(userId: String, createdAt: String): FeedbackEntity?

    /**
     * Gets a feedback entry by its primary key ID.
     */
    @Query("SELECT * FROM feedback WHERE feedbackId = :feedbackId LIMIT 1")
    suspend fun getFeedbackById(feedbackId: Long): FeedbackEntity?

    /**
     * Returns a chronological stream of all system feedback.
     * Ordered by createdAt string (ISO 8601).
     */
    @Query("SELECT * FROM feedback ORDER BY createdAt DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>
}
