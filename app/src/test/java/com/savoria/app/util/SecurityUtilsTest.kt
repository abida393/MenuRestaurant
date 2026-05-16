package com.savoria.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class SecurityUtilsTest {

    @Test
    fun hashPassword_producesBcryptFormat() {
        val hash = SecurityUtils.hashPassword("test-password")
        assertTrue(hash.startsWith("$2"))
        assertTrue(hash.length > 50)
    }

    @Test
    fun hashPassword_usesUniqueSaltPerCall() {
        val first = SecurityUtils.hashPassword("same-password")
        val second = SecurityUtils.hashPassword("same-password")
        assertNotEquals(first, second)
        assertTrue(SecurityUtils.verifyPassword("same-password", first))
        assertTrue(SecurityUtils.verifyPassword("same-password", second))
    }

    @Test
    fun verifyPassword_rejectsWrongPassword() {
        val hash = SecurityUtils.hashPassword("correct")
        assertFalse(SecurityUtils.verifyPassword("wrong", hash))
    }

    @Test
    fun verifyPassword_acceptsLegacySha256Hash() {
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest("legacy-pass".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertTrue(SecurityUtils.verifyPassword("legacy-pass", legacy))
        assertTrue(SecurityUtils.needsRehash(legacy))
    }

    @Test
    fun needsRehash_falseForBcrypt() {
        val hash = SecurityUtils.hashPassword("secure")
        assertFalse(SecurityUtils.needsRehash(hash))
    }
}
