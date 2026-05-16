package com.savoria.app.util

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest

object SecurityUtils {

    private const val BCRYPT_COST = 12

    /** Returns a BCrypt hash (includes a unique salt per call). */
    fun hashPassword(password: String): String =
        BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())

    /** Verifies a plaintext password against a BCrypt or legacy SHA-256 (hex) hash. */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return if (isBcryptHash(storedHash)) {
            BCrypt.verifyer().verify(password.toCharArray(), storedHash.toCharArray()).verified
        } else {
            legacySha256Hex(password) == storedHash
        }
    }

    /** True when the stored hash should be replaced with BCrypt (e.g. after legacy login). */
    fun needsRehash(storedHash: String): Boolean = !isBcryptHash(storedHash)

    private fun isBcryptHash(storedHash: String): Boolean =
        storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")

    /** Legacy SHA-256 (no salt). Used only to verify and migrate existing rows. */
    private fun legacySha256Hex(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
