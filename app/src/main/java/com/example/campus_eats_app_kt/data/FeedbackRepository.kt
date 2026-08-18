package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * FeedbackRepository handles the collection and categorization of user feedback.
 * Integrates Realtime Database for online collection.
 */
class FeedbackRepository(
    private val feedbackDao: FeedbackDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabaseProvider.instance
)
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
        val feedback = FeedbackEntity(
            userId = userId,
            subject = subject,
            message = message,
            type = type
        )

        // 1. Persist locally
        feedbackDao.insertFeedback(feedback)

        // 2. Sync to RTDB
        try
        {
            connectivityManager.ensureInternet()
            firebaseDatabase.getReference("feedback").push().setValue(feedback).await()
        }
        catch (_: Exception)
        {
            // Fail silently locally if offline, but inform UI if required via exception
        }
    }
}
