package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.entity.FeedbackEntity

/**
 * FeedbackKeys generates deterministic, idempotent Firebase keys for user feedback.
 * Prevents key collision and duplicate record creation across devices.
 */
object FeedbackKeys
{
    /**
     * Constructs a deterministic, URL-safe key from the user ID and timestamp.
     */
    fun createKey(userId: String, createdAt: String): String
    {
        val sanitizedUser = userId.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val sanitizedTime = createdAt.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return "${sanitizedUser}_${sanitizedTime}"
    }

    /**
     * Extracts the deterministic key from a FeedbackEntity.
     */
    fun keyFor(feedback: FeedbackEntity): String
    {
        return createKey(feedback.userId, feedback.createdAt)
    }
}
