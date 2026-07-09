package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * FeedbackRepository handles the collection and categorization of user feedback.
 */
class FeedbackRepository(private val feedbackDao: FeedbackDao)
{
    /**
     * Retrieves all feedback entries from the database.
     */
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    /**
     * Filters feedback to return only complaints.
     */
    fun getComplaints(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.COMPLAINT } }

    /**
     * Filters feedback to return only compliments.
     */
    fun getCompliments(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.COMPLIMENT } }

    /**
     * Persists a new feedback entry.
     */
    suspend fun submitFeedback(userId: String, subject: String, message: String, type: FeedbackType)
    {
        feedbackDao.insertFeedback(
            FeedbackEntity(
                userId = userId,
                subject = subject,
                message = message,
                type = type
            )
        )
    }
}
