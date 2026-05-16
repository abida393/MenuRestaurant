package com.savoria.app.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DishDrawableResourcesTest {

    @Test
    fun byName_containsAllSeededDishKeys() {
        val expected = listOf(
            "dish_wellington",
            "dish_scallops",
            "dish_pappardelle",
            "dish_lava_sphere",
            "dish_burrata",
            "dish_lamb"
        )
        expected.forEach { key ->
            assertTrue("$key should be registered", DishDrawableResources.byName.containsKey(key))
        }
    }

    @Test
    fun resIdForKey_unknownKey_returnsPlaceholderId() {
        val placeholder = DishDrawableResources.resIdForKey("dish_placeholder")
        assertEquals(placeholder, DishDrawableResources.resIdForKey("not_a_real_dish_key"))
    }

    @Test
    fun resIdForKey_knownKeys_haveDistinctIds() {
        val wellington = DishDrawableResources.resIdForKey("dish_wellington")
        val scallops = DishDrawableResources.resIdForKey("dish_scallops")
        assertNotEquals(wellington, scallops)
    }
}
