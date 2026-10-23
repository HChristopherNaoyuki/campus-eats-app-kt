package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * FeedbackKeysTest verifies deterministic Firebase key generation for feedback items.
 */
class FeedbackKeysTest
{
    @Test
    fun createKey_generatesDeterministicUrlSafeKey()
    {
        val userId = "U101-ABCD-1234"
        val createdAt = "2026-10-20T14:30:00Z"

        val key1 = FeedbackKeys.createKey(userId, createdAt)
        val key2 = FeedbackKeys.createKey(userId, createdAt)

        assertEquals(key1, key2)
        assertEquals("U101_ABCD_1234_2026_10_20T14_30_00Z", key1)
    }

    @Test
    fun keyFor_extractsKeyFromFeedbackEntity()
    {
        val feedback = FeedbackEntity(
            feedbackId = 1L,
            userId = "U202",
            type = FeedbackType.COMPLIMENT,
            subject = "Great Service",
            message = "The order arrived fast and hot.",
            userName = "Alice",
            userEmail = "alice@example.com",
            status = FeedbackStatus.PENDING,
            createdAt = "2026-10-21T09:15:00Z",
            updatedAt = "2026-10-21T09:15:00Z",
        )

        val key = FeedbackKeys.keyFor(feedback)

        assertEquals("U202_2026_10_21T09_15_00Z", key)
    }
}
