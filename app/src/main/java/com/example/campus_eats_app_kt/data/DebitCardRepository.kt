package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.DebitCardDao
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import kotlinx.coroutines.flow.Flow

class DebitCardRepository(private val debitCardDao: DebitCardDao)
{
    fun getCards(userId: String): Flow<List<DebitCardEntity>> =
        debitCardDao.getCardsByUserId(userId)

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

    suspend fun deleteCard(card: DebitCardEntity)
    {
        debitCardDao.deleteCard(card)
    }
}
