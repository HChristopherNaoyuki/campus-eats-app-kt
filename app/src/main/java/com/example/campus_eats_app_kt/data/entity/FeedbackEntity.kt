package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * FeedbackType defines the nature of user feedback.
 * Values are mapped to lowercase to match Firebase Realtime Database security rules.
 */
@Serializable
enum class FeedbackType
{
    @SerialName("complaint")
    COMPLAINT,
    @SerialName("compliment")
    COMPLIMENT
}

/**
 * FeedbackStatus defines the processing state of a feedback entry.
 * Values are mapped to lowercase to match Firebase Realtime Database security rules.
 */
@Serializable
enum class FeedbackStatus
{
    @SerialName("pending")
    PENDING,
    @SerialName("resolved")
    RESOLVED
}

/**
 * FeedbackEntity stores user-submitted reports or praise.
 * Fully compliant with Firebase Realtime Database validation rules for /feedback path.
 */
@Serializable
@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val feedbackId: Long = 0,
    val userId: String, // Authenticated Firebase UID
    val type: FeedbackType,
    val subject: String,
    val message: String,
    val userName: String,
    val userEmail: String,
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val createdAt: String, // ISO 8601 or non-empty string
    val updatedAt: String, // ISO 8601 or non-empty string
)
