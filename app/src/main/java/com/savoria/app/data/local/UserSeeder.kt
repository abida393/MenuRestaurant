package com.savoria.app.data.local

import com.savoria.app.data.local.dao.UserDao
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.local.entity.UserRole

object UserSeeder {

    const val ADMIN_EMAIL = "admin@savoria.com"
    const val CHEF_EMAIL = "chef@savoria.com"
    const val SERVEUR_EMAIL = "serveur@savoria.com"

    const val ADMIN_PASSWORD = "admin123"
    const val CHEF_PASSWORD = "chef123"
    const val SERVEUR_PASSWORD = "serveur123"

    fun hashPassword(raw: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun defaultUsers(): List<User> = listOf(
        User(
            id = "user-admin",
            nom = "Administrateur",
            email = ADMIN_EMAIL,
            password = hashPassword(ADMIN_PASSWORD),
            role = UserRole.ADMIN,
            actif = true
        ),
        User(
            id = "user-chef",
            nom = "Chef Pierre",
            email = CHEF_EMAIL,
            password = hashPassword(CHEF_PASSWORD),
            role = UserRole.CHEF,
            actif = true
        ),
        User(
            id = "user-serveur",
            nom = "Marie Serveur",
            email = SERVEUR_EMAIL,
            password = hashPassword(SERVEUR_PASSWORD),
            role = UserRole.SERVEUR,
            actif = true
        )
    )

    /** Insère les comptes manquants (ne duplique pas un e-mail déjà présent). */
    suspend fun ensureDefaultUsers(userDao: UserDao) {
        for (user in defaultUsers()) {
            if (userDao.getUserByEmail(user.email) == null) {
                userDao.insertUser(user)
            }
        }
    }
}
