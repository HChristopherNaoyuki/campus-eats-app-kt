package com.example.campus_eats_app_kt.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IdGeneratorTest verifies the generation of unique identifiers.
 */
class IdGeneratorTest
{
    /**
     * Finding 22: Test 16-character continuous format
     */
    @Test
    fun generateUserId_returnsCorrectFormat()
    {
        val userId = IdGenerator.generateUserId()
        val regex = Regex("^[A-Z0-9]{16}$")
        assertTrue("ID $userId should match the required format", regex.matches(userId))
    }

    @Test
    fun generateUserId_returnsCorrectLength()
    {
        val userId = IdGenerator.generateUserId()
        assertEquals(16, userId.length)
    }

    @Test
    fun generateUserId_producesUniqueValues()
    {
        val iterations = 100
        val ids = mutableSetOf<String>()
        repeat(iterations)
        {
            ids.add(IdGenerator.generateUserId())
        }
        assertEquals(iterations, ids.size)
    }

    /**
     * Finding 22: Test Order ID format
     */
    @Test
    fun generateOrderId_returnsCorrectFormat()
    {
        val orderId = IdGenerator.generateOrderId()
        val regex = Regex("^ORD-[0-9]{8}-[A-Z0-9]{8}$")
        assertTrue("Order ID $orderId should match required format", regex.matches(orderId))
    }
}
