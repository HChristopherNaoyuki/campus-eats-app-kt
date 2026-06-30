package com.example.campus_eats_app_kt.util

import java.util.UUID

object IdGenerator {
    /**
     * Generates a unique 16-character alphanumeric User ID in the format XXXX-XXXX-XXXX-XXXX.
     */
    fun generateUserId(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val charPool = allowedChars.shuffled()
        
        fun randomSegment(): String = (1..4)
            .map { kotlin.random.Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")

        return "${randomSegment()}-${randomSegment()}-${randomSegment()}-${randomSegment()}"
    }
}
