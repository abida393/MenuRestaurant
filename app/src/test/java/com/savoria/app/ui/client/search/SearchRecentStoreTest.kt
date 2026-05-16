package com.savoria.app.ui.client.search

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Logic-only checks for recent-query ordering (no Android context).
 */
class SearchRecentStoreTest {

    @Test
    fun recentList_dedupesAndCapsAtMax() {
        val merged = dedupeAndCap(
            existing = listOf("pasta", "beef", "fish", "a", "b", "c", "d"),
            incoming = "pasta",
            max = 8
        )
        assertEquals("pasta", merged.first())
        assertEquals(7, merged.size)
        assertEquals(1, merged.count { it == "pasta" })
    }

    @Test
    fun recentList_putsNewQueryFirst() {
        val merged = dedupeAndCap(existing = listOf("wine"), incoming = "dessert", max = 8)
        assertEquals(listOf("dessert", "wine"), merged)
    }

  private fun dedupeAndCap(existing: List<String>, incoming: String, max: Int): List<String> {
        val trimmed = incoming.trim()
        if (trimmed.isEmpty()) return existing
        return (listOf(trimmed) + existing.filter { it != trimmed }).take(max)
    }
}
