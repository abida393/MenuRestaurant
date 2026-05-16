package com.savoria.app.ui.viewmodel

import com.savoria.app.data.local.entity.Dish
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStatsTest {

    @Test
    fun dashboardStats_countsDishesCorrectly() {
        val dishes = listOf(
            dish(disponible = true, specialty = true, validated = true),
            dish(disponible = false, specialty = false, validated = false),
            dish(disponible = true, specialty = false, validated = true)
        )

        val stats = DashboardStats(
            totalDishes = dishes.size,
            chefSpecialties = dishes.count { it.isChefSpecial },
            unavailableDishes = dishes.count { !it.disponible },
            pendingValidation = dishes.count { !it.isValidatedByAdmin },
            totalCategories = 4
        )

        assertEquals(3, stats.totalDishes)
        assertEquals(1, stats.chefSpecialties)
        assertEquals(1, stats.unavailableDishes)
        assertEquals(1, stats.pendingValidation)
    }

    private fun dish(
        disponible: Boolean,
        specialty: Boolean,
        validated: Boolean
    ) = Dish(
        nom = "Test",
        categoryId = "Mains",
        prix = 10.0,
        photoUrl = "dish_test",
        disponible = disponible,
        isChefSpecialty = specialty,
        isValidatedByAdmin = validated
    )
}
