package com.example.campus_eats_app_kt.util

import kotlin.random.Random

/**
 * IdGenerator provides utility methods for generating unique identifiers within the system.
 */
object IdGenerator
{
    private val ALLOWED_CHARS = ('A'..'Z') + ('0'..'9')

    /**
     * Generates a unique 16-character alphanumeric User ID in the format XXXX-XXXX-XXXX-XXXX.
     * This format is optimized for human readability and manual account recovery.
     */
    fun generateUserId(): String
    {
        /**
         * Generates a single 4-character alphanumeric segment.
         */
        fun randomSegment(): String
        {
            return (1..4)
                .map { ALLOWED_CHARS[Random.nextInt(ALLOWED_CHARS.size)] }
                .joinToString("")
        }

        return "${randomSegment()}-${randomSegment()}-${randomSegment()}-${randomSegment()}"
    }
}
