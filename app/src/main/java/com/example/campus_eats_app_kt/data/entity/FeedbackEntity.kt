package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FeedbackType
{
    COMPLAINT,
    COMPLIMENT
}

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val feedbackId: Long = 0,
    val userId: String,
    val subject: String,
    val message: String,
    val type: FeedbackType,
    val timestamp: Long = System.currentTimeMillis()
)
