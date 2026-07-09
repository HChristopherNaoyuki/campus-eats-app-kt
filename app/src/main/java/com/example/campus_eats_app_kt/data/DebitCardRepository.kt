package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.DebitCardDao
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * DebitCardRepository manages the persistence of payment card details for students.
 */
class DebitCardRepository(private val debitCardDao: DebitCardDao)
{
    /**
     * Retrieves all saved cards for a specific user.
     */
    fun getCards(userId: String): Flow<List<DebitCardEntity>> =
        debitCardDao.getCardsByUserId(userId)

    /**
     * Persists new card information.
     */
    suspend fun addCard(userId: String, cardNumber: String, expiryDate: String, cvv: String)
    {
        debitCardDao.insertCard(
            DebitCardEntity(
                userId = userId,
                cardNumber = cardNumber,
                expiryDate = expiryDate,
                cvv = cvv
            )
        )
    }

    /**
     * Removes a card from the user's profile.
     */
    suspend fun deleteCard(card: DebitCardEntity)
    {
        debitCardDao.deleteCard(card)
    }
}
