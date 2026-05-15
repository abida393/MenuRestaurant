package com.savoria.app.data.local

import android.content.Context
import java.util.UUID

object ClientSessionManager {
    private const val PREFS = "savoria_client_prefs"
    private const val KEY_SESSION = "client_session_id"

    fun getSessionId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_SESSION, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_SESSION, id).apply()
        }
        return id
    }
}
