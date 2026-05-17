package com.savoria.app.data.local

import android.content.Context
import com.savoria.app.data.local.dao.UserDao
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.local.entity.UserRole
import com.savoria.app.util.SecurityUtils

object UserSeeder {

    const val ADMIN_EMAIL = "admin@savoria.com"
    const val CHEF_EMAIL = "chef@savoria.com"
    const val SERVEUR_EMAIL = "serveur@savoria.com"

    const val DEFAULT_ADMIN_PASSWORD = "admin123"
    const val DEFAULT_CHEF_PASSWORD = "chef123"
    const val DEFAULT_SERVEUR_PASSWORD = "serveur123"

    private data class UserTemplate(
        val id: String,
        val nom: String,
        val email: String,
        val role: UserRole,
        val defaultPassword: String
    )

    private fun defaultUserTemplates(): List<UserTemplate> = listOf(
        UserTemplate(
            "user-admin",
            "Administrateur",
            ADMIN_EMAIL,
            UserRole.ADMIN,
            DEFAULT_ADMIN_PASSWORD
        ),
        UserTemplate(
            "user-chef",
            "Chef Pierre",
            CHEF_EMAIL,
            UserRole.CHEF,
            DEFAULT_CHEF_PASSWORD
        ),
        UserTemplate(
            "user-serveur",
            "Marie Serveur",
            SERVEUR_EMAIL,
            UserRole.SERVEUR,
            DEFAULT_SERVEUR_PASSWORD
        )
    )

    fun defaultPasswordFor(email: String): String? = when (email.lowercase()) {
        ADMIN_EMAIL -> DEFAULT_ADMIN_PASSWORD
        CHEF_EMAIL -> DEFAULT_CHEF_PASSWORD
        SERVEUR_EMAIL -> DEFAULT_SERVEUR_PASSWORD
        else -> null
    }

    /**
     * Crée les comptes staff par défaut s'ils n'existent pas encore.
     * Les mots de passe ne sont jamais réinitialisés automatiquement :
     * l'administrateur peut les modifier dans Gestion des utilisateurs.
     */
    suspend fun ensureStaffAccounts(@Suppress("UNUSED_PARAMETER") context: Context, userDao: UserDao) {
        for (template in defaultUserTemplates()) {
            if (userDao.getUserByEmail(template.email) == null) {
                userDao.insertUser(template.toUser())
            }
        }
    }

    private fun UserTemplate.toUser(): User = User(
        id = id,
        nom = nom,
        email = email,
        password = SecurityUtils.hashPassword(defaultPassword),
        role = role,
        actif = true
    )
}
