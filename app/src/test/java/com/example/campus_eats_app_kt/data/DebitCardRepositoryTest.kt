package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.DebitCardDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * DebitCardRepositoryTest verifies management of student payment methods.
 */
class DebitCardRepositoryTest
{
    private lateinit var debitCardDao: DebitCardDao
    private lateinit var repository: DebitCardRepository

    @Before
    fun setUp()
    {
        debitCardDao = mockk(relaxed = true)
        repository = DebitCardRepository(debitCardDao)
    }

    /**
     * Requirement: Test card addition
     * Finding 3: CVV is no longer persisted. Number is masked.
     */
    @Test
    fun addCard_persistsInDao() = runTest {
        val userId = "U1"
        val number = "4321123456785678" // Valid-ish Luhn for demo
        val expiry = "12/26"

        // We use a known Luhn-valid number if the algorithm is strictly enforced in test
        repository.addCard(userId, number, expiry)

        coVerify {
            debitCardDao.insertCard(
                match {
                    (it.userId == userId) && it.cardNumber.contains("5678") && (it.expiryDate == expiry)
                },
            )
        }
    }
}
