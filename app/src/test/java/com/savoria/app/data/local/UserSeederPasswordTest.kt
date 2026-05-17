package com.savoria.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserSeederPasswordTest {

    @Test
    fun defaultPasswordFor_admin_returnsAdmin123() {
        assertEquals("admin123", UserSeeder.defaultPasswordFor(UserSeeder.ADMIN_EMAIL))
    }

    @Test
    fun defaultPasswordFor_chef_returnsChef123() {
        assertEquals("chef123", UserSeeder.defaultPasswordFor(UserSeeder.CHEF_EMAIL))
    }

    @Test
    fun defaultPasswordFor_serveur_returnsServeur123() {
        assertEquals("serveur123", UserSeeder.defaultPasswordFor(UserSeeder.SERVEUR_EMAIL))
    }

    @Test
    fun defaultPasswordFor_unknownEmail_returnsNull() {
        assertNull(UserSeeder.defaultPasswordFor("other@savoria.com"))
    }
}
