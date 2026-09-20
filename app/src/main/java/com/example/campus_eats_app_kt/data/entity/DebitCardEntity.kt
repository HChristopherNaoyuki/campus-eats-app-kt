package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * DebitCardEntity stores masked debit card information for offline reference.
 * Finding 3: CVV storage is prohibited for PCI compliance. Only the last 4 digits are retained.
 */
@Serializable
@Entity(tableName = "debit_cards")
data class DebitCardEntity(
    @PrimaryKey(autoGenerate = true)
    val cardId: Long = 0,
    val userId: String,
    val cardNumber: String, // Masked (e.g., **** **** **** 1234)
    val expiryDate: String,
)
