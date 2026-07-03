package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FeedbackRepository(private val feedbackDao: FeedbackDao)
{
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    fun getComplaints(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.COMPLAINT } }

    fun getCompliments(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.COMPLIMENT } }

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
