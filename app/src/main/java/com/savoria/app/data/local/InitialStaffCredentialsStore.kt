package com.savoria.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.savoria.app.data.local.entity.UserRole
import org.json.JSONArray
import org.json.JSONObject

/**
 * Holds generated staff passwords in encrypted prefs until the admin acknowledges the dialog.
 * Plaintext is never written to the APK and is cleared after acknowledgment.
 */
class InitialStaffCredentialsStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_FILE,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPending(): Boolean = getPending().isNotEmpty()

    fun getPending(): List<SeededStaffCredential> {
        val raw = prefs.getString(KEY_PENDING_JSON, null) ?: return emptyList()
        return parseCredentials(raw)
    }

    fun mergePending(credentials: List<SeededStaffCredential>) {
        if (credentials.isEmpty()) return
        val merged = getPending()
            .associateBy { it.email }
            .toMutableMap()
        credentials.forEach { merged[it.email] = it }
        prefs.edit().putString(KEY_PENDING_JSON, serializeCredentials(merged.values.toList())).apply()
    }

    fun clearPending() {
        prefs.edit().remove(KEY_PENDING_JSON).apply()
    }

    private fun serializeCredentials(credentials: List<SeededStaffCredential>): String {
        val array = JSONArray()
        credentials.forEach { cred ->
            array.put(
                JSONObject()
                    .put("email", cred.email)
                    .put("role", cred.role.name)
                    .put("displayName", cred.displayName)
                    .put("password", cred.plainPassword)
            )
        }
        return array.toString()
    }

    private fun parseCredentials(json: String): List<SeededStaffCredential> {
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    SeededStaffCredential(
                        email = obj.getString("email"),
                        role = UserRole.valueOf(obj.getString("role")),
                        displayName = obj.getString("displayName"),
                        plainPassword = obj.getString("password")
                    )
                )
            }
        }
    }

    companion object {
        private const val PREFS_FILE = "staff_initial_credentials"
        private const val KEY_PENDING_JSON = "pending_credentials_json"
    }
}
