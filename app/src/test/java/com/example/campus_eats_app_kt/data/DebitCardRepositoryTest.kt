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
     */
    @Test
    fun addCard_persistsInDao() = runTest {
        val userId = "U1"
        val number = "1234567812345678"
        val expiry = "12/26"
        val cvv = "123"

        repository.addCard(userId, number, expiry, cvv)

        coVerify {
            debitCardDao.insertCard(match {
                it.userId == userId && it.cardNumber == number && it.expiryDate == expiry && it.cvv == cvv
            })
        }
    }
}
