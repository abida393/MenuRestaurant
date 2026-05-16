package com.savoria.app.data.local

import android.content.Context
import com.savoria.app.data.local.entity.UserRole

object StaffSessionManager {
    private const val PREFS = "savoria_staff_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_LOGGED_IN = "logged_in"

    fun saveSession(context: Context, userId: String, role: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun clearSession(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOGGED_IN, false)

    fun getUserId(context: Context): String? =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)

    fun getUserRole(context: Context): String? =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_USER_ROLE, null)

    fun isAdmin(context: Context): Boolean =
        isLoggedIn(context) && getUserRole(context) == UserRole.ADMIN.name

    fun isChef(context: Context): Boolean =
        isLoggedIn(context) && getUserRole(context) == UserRole.CHEF.name

    fun isServeur(context: Context): Boolean =
        isLoggedIn(context) && getUserRole(context) == UserRole.SERVEUR.name
}
