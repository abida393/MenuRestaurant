package com.savoria.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSeederPasswordTest {

    @Test
    fun generateSecurePassword_hasExpectedLength() {
        val password = UserSeeder.generateSecurePassword()
        assertEquals(16, password.length)
    }

    @Test
    fun generateSecurePassword_usesAllowedAlphabet() {
        val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%&*".toSet()
        repeat(20) {
            val password = UserSeeder.generateSecurePassword()
            assertTrue(password.all { it in allowed })
        }
    }

    @Test
    fun generateSecurePassword_producesDistinctValues() {
        val passwords = (1..10).map { UserSeeder.generateSecurePassword() }.toSet()
        assertTrue(passwords.size > 1)
    }
}
