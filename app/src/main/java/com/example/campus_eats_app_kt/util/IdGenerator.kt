package com.example.campus_eats_app_kt.util

import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * IdGenerator provides utility methods for generating unique identifiers within the system.
 * Finding 22: Hardened to use java.security.SecureRandom for cryptographic security.
 * Aligned to exactly 16 continuous alphanumeric characters as per process document.
 */
object IdGenerator
{
    private val ALLOWED_CHARS = ('A'..'Z') + ('0'..'9')
    private const val USER_ID_LENGTH = 16
    private val secureRandom = SecureRandom()

    /**
     * Generates a unique 16-character User ID.
     * 
     * @return A randomly generated alphanumeric User ID.
     */
    fun generateUserId(): String
    {
        return (1..USER_ID_LENGTH)
            .map()
            {
                ALLOWED_CHARS[secureRandom.nextInt(ALLOWED_CHARS.size)]
            }
            .joinToString("")
    }

    /**
     * Generates an Order ID in the format ORD-YYYYMMDD-XXXXXXXX.
     * Finding 22: Complies with process document section 11.2.
     */
    fun generateOrderId(): String
    {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        val datePart = sdf.format(Date())
        
        val randomPart = (1..8)
            .map()
            {
                ALLOWED_CHARS[secureRandom.nextInt(ALLOWED_CHARS.size)]
            }
            .joinToString("")
            
        return "ORD-$datePart-$randomPart"
    }
}
