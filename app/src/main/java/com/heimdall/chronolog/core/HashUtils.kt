package com.heimdall.chronolog.core

import java.security.MessageDigest

object HashUtils {

    /**
     * Generates the SHA-256 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-256 hash as a hexadecimal string, or null if an error occurs.
     */
    fun sha256(input: String): String? {
        return try {
            val bytes = input.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
