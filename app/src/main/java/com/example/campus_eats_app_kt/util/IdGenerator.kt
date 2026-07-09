package com.example.campus_eats_app_kt.util

import kotlin.random.Random

/**
 * IdGenerator provides utility methods for generating unique identifiers within the system.
 */
object IdGenerator
{
    private val ALLOWED_CHARS = ('A'..'Z') + ('0'..'9')
    private const val SEGMENT_LENGTH = 4
    private const val SEGMENT_COUNT = 4

    /**
     * Generates a unique 16-character alphanumeric User ID in the format XXXX-XXXX-XXXX-XXXX.
     * This format is optimized for human readability and manual account recovery.
     * 
     * @return A randomly generated, formatted User ID.
     */
    fun generateUserId(): String
    {
        val segments = mutableListOf<String>()

        repeat(SEGMENT_COUNT)
        {
            segments.add(generateRandomSegment())
        }

        return segments.joinToString("-")
    }

    /**
     * Generates a single alphanumeric segment of standard length.
     * 
     * @return A random alphanumeric string.
     */
    private fun generateRandomSegment(): String
    {
        return (1..SEGMENT_LENGTH)
            .map { ALLOWED_CHARS[Random.nextInt(ALLOWED_CHARS.size)] }
            .joinToString("")
    }
}
