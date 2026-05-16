package com.savoria.app.data.local

import android.content.Context
import com.savoria.app.data.local.dao.UserDao
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.local.entity.UserRole
import com.savoria.app.util.SecurityUtils
import java.security.SecureRandom

object UserSeeder {

    const val ADMIN_EMAIL = "admin@savoria.com"
    const val CHEF_EMAIL = "chef@savoria.com"
    const val SERVEUR_EMAIL = "serveur@savoria.com"

    private const val PREFS_SECURE_SEEDING = "staff_secure_seeding"
    private const val KEY_SECURE_SEEDING_DONE = "secure_seeding_done"

    private const val PASSWORD_LENGTH = 16
    private const val PASSWORD_ALPHABET =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%&*"

    private data class UserTemplate(
        val id: String,
        val nom: String,
        val email: String,
        val role: UserRole
    )

    private fun defaultUserTemplates(): List<UserTemplate> = listOf(
        UserTemplate("user-admin", "Administrateur", ADMIN_EMAIL, UserRole.ADMIN),
        UserTemplate("user-chef", "Chef Pierre", CHEF_EMAIL, UserRole.CHEF),
        UserTemplate("user-serveur", "Marie Serveur", SERVEUR_EMAIL, UserRole.SERVEUR)
    )

    fun generateSecurePassword(): String {
        val random = SecureRandom()
        return buildString(PASSWORD_LENGTH) {
            repeat(PASSWORD_LENGTH) {
                append(PASSWORD_ALPHABET[random.nextInt(PASSWORD_ALPHABET.length)])
            }
        }
    }

    /**
     * Ensures default staff accounts exist with random passwords.
     * On legacy installs, rotates factory default accounts once and stores credentials for display.
     */
    suspend fun ensureStaffAccounts(context: Context, userDao: UserDao) {
        val prefs = context.applicationContext.getSharedPreferences(
            PREFS_SECURE_SEEDING,
            Context.MODE_PRIVATE
        )
        val credentialsStore = InitialStaffCredentialsStore(context)
        val pending = mutableListOf<SeededStaffCredential>()
        val templates = defaultUserTemplates()

        for (template in templates) {
            if (userDao.getUserByEmail(template.email) == null) {
                val plainPassword = generateSecurePassword()
                userDao.insertUser(template.toUser(plainPassword))
                pending.add(template.toCredential(plainPassword))
            }
        }

        val secureSeedingDone = prefs.getBoolean(KEY_SECURE_SEEDING_DONE, false)
        if (!secureSeedingDone && pending.size < templates.size) {
            for (template in templates) {
                val existing = userDao.getUserByEmail(template.email) ?: continue
                if (existing.id != template.id) continue
                val plainPassword = generateSecurePassword()
                userDao.updateUser(existing.copy(password = SecurityUtils.hashPassword(plainPassword)))
                pending.add(template.toCredential(plainPassword))
            }
        }

        if (pending.isNotEmpty() || !secureSeedingDone) {
            prefs.edit().putBoolean(KEY_SECURE_SEEDING_DONE, true).apply()
        }
        if (pending.isNotEmpty()) {
            credentialsStore.mergePending(pending)
        }
    }

    private fun UserTemplate.toUser(plainPassword: String): User = User(
        id = id,
        nom = nom,
        email = email,
        password = SecurityUtils.hashPassword(plainPassword),
        role = role,
        actif = true
    )

    private fun UserTemplate.toCredential(plainPassword: String): SeededStaffCredential =
        SeededStaffCredential(
            email = email,
            role = role,
            displayName = nom,
            plainPassword = plainPassword
        )
}
