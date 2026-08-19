package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * FeedbackRepositoryTest verifies user feedback submission and categorization.
 */
class FeedbackRepositoryTest
{
    private lateinit var feedbackDao: FeedbackDao
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var repository: FeedbackRepository

    @Before
    fun setUp()
    {
        feedbackDao = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)

        // Mock successful RTDB task completion
        val task = mockk<Task<Void>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns null

        val ref = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDatabase.getReference(any()) } returns ref
        every { ref.child(any()) } returns ref
        every { ref.push() } returns ref
        every { ref.setValue(any()) } returns task

        repository = FeedbackRepository(feedbackDao, connectivityManager, firebaseDatabase)
    }

    /**
     * Requirement: Test feedback submission
     */
    @Test
    fun submitFeedback_persistsInDao()
    {
        runTest()
        {
            val userId = "U1"
            val subject = "Subject"
            val message = "Message"
            val type = FeedbackType.COMPLIMENT

            repository.submitFeedback(userId, subject, message, type)

            coVerify {
                feedbackDao.insertFeedback(match {
                    it.userId == userId && it.subject == subject && it.message == message && it.type == type
                })
            }
        }
    }
}
