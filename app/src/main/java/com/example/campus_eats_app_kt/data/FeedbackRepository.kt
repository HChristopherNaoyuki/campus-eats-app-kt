package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * FeedbackRepository handles the collection and categorization of user feedback.
 * Integrates Realtime Database for online collection.
 * Enforces compliance with /feedback path security rules.
 */
class FeedbackRepository(
    private val feedbackDao: FeedbackDao,
    private val userDao: UserDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase,
)
{
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply()
    {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Retrieves all feedback entries from the database.
     */
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    /**
     * Filters feedback to return only complaints.
     */
    fun getComplaints(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.complaint } }

    /**
     * Filters feedback to return only compliments.
     */
    fun getCompliments(): Flow<List<FeedbackEntity>> =
        feedbackDao.getAllFeedback()
            .map { list -> list.filter { it.type == FeedbackType.compliment } }

    /**
     * Persists a new feedback entry with all mandatory fields required by Firebase rules.
     */
    suspend fun submitFeedback(userId: String, subject: String, message: String, type: FeedbackType)
    {
        val user = userDao.getUserById(userId) ?: throw Exception("User profile not found.")
        val now = dateFormat.format(Date())

        val feedback = FeedbackEntity(
            userId = userId,
            type = type,
            subject = subject,
            message = message,
            userName = user.fullName,
            userEmail = user.email,
            status = FeedbackStatus.pending,
            createdAt = now,
            updatedAt = now,
        )

        // 1. Persist locally
        feedbackDao.insertFeedback(feedback)

        // 2. Sync to RTDB
        try
        {
            connectivityManager.ensureInternet()
            // Firebase validation requires all fields present
            firebaseDatabase.getReference("feedback").push().setValue(feedback).await()
        }
        catch (e: Exception)
        {
            // Map technical error to user-friendly string
            throw Exception(FirebaseExceptionHandler.parse(e))
        }
    }
}
