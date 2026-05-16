package com.savoria.app.ui.client.search

import android.content.Context

object SearchRecentStore {

    private const val PREFS = "savoria_search_prefs"
    private const val KEY_RECENT = "recent_queries"
    private const val DELIMITER = "\u001E"
    private const val MAX_ENTRIES = 8

    fun getRecent(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_RECENT, null) ?: return emptyList()
        return raw.split(DELIMITER).filter { it.isNotBlank() }
    }

    fun addQuery(context: Context, query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val updated = listOf(trimmed) + getRecent(context).filter { it != trimmed }
        prefs(context).edit()
            .putString(KEY_RECENT, updated.take(MAX_ENTRIES).joinToString(DELIMITER))
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_RECENT).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
