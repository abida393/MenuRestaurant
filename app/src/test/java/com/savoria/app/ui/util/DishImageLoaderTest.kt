package com.savoria.app.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DishImageLoaderTest {

    @Test
    fun isRemoteUrl_detectsHttpAndHttps() {
        assertTrue(DishImageLoader.isRemoteUrl("https://cdn.example.com/dish.jpg"))
        assertTrue(DishImageLoader.isRemoteUrl("http://example.com/a.png"))
    }

    @Test
    fun resolveModel_remoteUrl_returnsUrlString() {
        val url = "https://cdn.example.com/plat.png"
        assertEquals(url, DishImageLoader.resolveModel(url))
    }

    @Test
    fun resolveModel_localKey_usesDrawableMap() {
        val resolved = DishImageLoader.resolveModel("dish_lamb")
        assertEquals(DishDrawableResources.resIdForKey("dish_lamb"), resolved)
    }

    @Test
    fun resolveModel_empty_returnsPlaceholderDrawable() {
        assertEquals(
            DishDrawableResources.resIdForKey("dish_placeholder"),
            DishImageLoader.resolveModel("")
        )
    }
}
