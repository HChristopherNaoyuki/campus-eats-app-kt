package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * FeedbackRepository handles the collection and categorization of user feedback.
 * Finding 5: Hardened for crash resilience with input validation and safe network sync.
 */
class FeedbackRepository(
    private val feedbackDao: FeedbackDao,
    private val userDao: UserDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase,
)
{
    private fun getDateFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply()
    {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Retrieves all feedback entries from the database.
     */
    fun getAllFeedback(): Flow<List<FeedbackEntity>> = feedbackDao.getAllFeedback()

    /**
     * Persists a new feedback entry.
     * Enforces validation and authoritative cloud synchronization.
     */
    suspend fun submitFeedback(userId: String, subject: String, message: String, type: FeedbackType)
    {
        // Finding 5: Input validation
        if (subject.length < 3) throw Exception("Subject must be at least 3 characters.")
        if (message.length < 10) throw Exception("Feedback details must be at least 10 characters.")

        val user = userDao.getUserById(userId) ?: throw Exception("User profile not found.")
        val now = getDateFormat().format(Date())

        val feedback = FeedbackEntity(
            userId = userId,
            type = type,
            subject = subject,
            message = message,
            userName = user.fullName,
            userEmail = user.email,
            status = FeedbackStatus.PENDING,
            createdAt = now,
            updatedAt = now,
        )

        // 1. Persist locally first as a "Pending" record
        feedbackDao.insertFeedback(feedback)

        // 2. Authoritative Sync to RTDB
        try
        {
            connectivityManager.ensureInternet()
            // Finding 6: Use explicit mapping for Firebase compatibility
            firebaseDatabase.getReference("feedback").push()
                .setValue(mapFeedbackToMap(feedback))
                .await()
        }
        catch (_: Exception)
        {
            // Finding 5: Map failure to user-friendly error but maintain local record
            throw Exception("Offline mode: Feedback saved locally and will sync later.")
        }
    }

    /**
     * Finding 6: Manual mapper to ensure correct field naming and types in RTDB.
     */
    private fun mapFeedbackToMap(feedback: FeedbackEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to feedback.userId,
            "type" to feedback.type.name.lowercase(),
            "subject" to feedback.subject,
            "message" to feedback.message,
            "userName" to feedback.userName,
            "userEmail" to feedback.userEmail,
            "status" to feedback.status.name,
            "createdAt" to feedback.createdAt,
            "updatedAt" to feedback.updatedAt,
        )
    }

}
