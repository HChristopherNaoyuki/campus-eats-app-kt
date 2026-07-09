package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import io.mockk.coVerify
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
    private lateinit var repository: FeedbackRepository

    @Before
    fun setUp()
    {
        feedbackDao = mockk(relaxed = true)
        repository = FeedbackRepository(feedbackDao)
    }

    /**
     * Requirement: Test feedback submission
     */
    @Test
    fun submitFeedback_persistsInDao() = runTest {
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
