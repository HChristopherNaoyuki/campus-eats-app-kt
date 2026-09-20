package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.DebitCardDao
import com.example.campus_eats_app_kt.data.entity.DebitCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * DebitCardRepository manages the persistence of payment card details.
 * Finding 3: Hardened for PCI compliance by enforcing Luhn validation and masking.
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
     * Requirement: CVV is never stored. Card numbers are masked.
     */
    suspend fun addCard(userId: String, cardNumber: String, expiryDate: String)
    {
        val cleanNumber = cardNumber.replace(Regex("[^0-9]"), "")
        
        if (cleanNumber.length < 13 || !validateLuhn(cleanNumber))
        {
            throw Exception("Invalid debit card number.")
        }
        
        val masked = "**** **** **** ${cleanNumber.takeLast(4)}"
        
        debitCardDao.insertCard(
            DebitCardEntity(
                userId = userId,
                cardNumber = masked,
                expiryDate = expiryDate,
            ),
        )
    }

    /**
     * Luhn Algorithm for card number validation.
     */
    private fun validateLuhn(number: String): Boolean
    {
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0)
        {
            var n = Character.getNumericValue(number[i])
            if (alternate)
            {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    /**
     * Removes a card from the user's profile.
     */
    @Suppress("unused")
    suspend fun deleteCard(card: DebitCardEntity)
    {
        debitCardDao.deleteCard(card)
    }
}
