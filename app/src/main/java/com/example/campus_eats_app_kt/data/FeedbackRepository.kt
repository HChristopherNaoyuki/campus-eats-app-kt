package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import kotlinx.coroutines.flow.Flow

class FeedbackRepository(private val feedbackDao: FeedbackDao)
{
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    suspend fun submitFeedback(userId: String, subject: String, message: String)
    {
        feedbackDao.insertFeedback(
            FeedbackEntity(
                userId = userId,
                subject = subject,
                message = message
            )
        )
    }
}
