package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "debit_cards")
data class DebitCardEntity(
    @PrimaryKey(autoGenerate = true)
    val cardId: Long = 0,
    val userId: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String
)
