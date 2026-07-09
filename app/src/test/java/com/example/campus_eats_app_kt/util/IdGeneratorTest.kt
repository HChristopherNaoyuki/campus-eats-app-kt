package com.example.campus_eats_app_kt.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IdGeneratorTest verifies the generation of unique, correctly formatted User IDs.
 */
class IdGeneratorTest
{
    /**
     * Requirement: Test format (XXXX-XXXX-XXXX-XXXX)
     */
    @Test
    fun generateUserId_returnsCorrectFormat()
    {
        // When: Generating an ID
        val userId = IdGenerator.generateUserId()

        // Then: It matches the pattern of 4 segments separated by dashes
        val regex = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        assertTrue("ID $userId should match the required format", regex.matches(userId))
    }

    /**
     * Requirement: Test length (16 characters before formatting, 19 with dashes)
     */
    @Test
    fun generateUserId_returnsCorrectLength()
    {
        // When: Generating an ID
        val userId = IdGenerator.generateUserId()

        // Then: Total length including dashes is 19
        assertEquals(19, userId.length)
    }

    /**
     * Requirement: Test uniqueness
     */
    @Test
    fun generateUserId_producesUniqueValues()
    {
        // Given: Multiple generations
        val iterations = 100
        val ids = mutableSetOf<String>()

        // When: Generating IDs
        repeat(iterations)
        {
            ids.add(IdGenerator.generateUserId())
        }

        // Then: All IDs in the set are unique (size matches iterations)
        assertEquals(iterations, ids.size)
    }
}
