package com.savoria.app.ui.viewmodel

import com.savoria.app.data.local.model.DishSalesAggregate
import org.junit.Assert.assertEquals
import org.junit.Test

class TopDishesMappingTest {

    @Test
    fun mapAggregatesToDishStats_preservesRankOrderAndCounts() {
        val aggregates = listOf(
            DishSalesAggregate(dishId = "b", totalQuantity = 10),
            DishSalesAggregate(dishId = "a", totalQuantity = 5)
        )
        val dishes = mapOf(
            "a" to FakeDish(id = "a", nom = "Alpha"),
            "b" to FakeDish(id = "b", nom = "Bravo")
        )

        val stats = aggregates.mapNotNull { row ->
            val dish = dishes[row.dishId] ?: return@mapNotNull null
            DishStat(
                dishId = dish.id,
                nom = dish.nom,
                categoryId = null,
                prixFormat = "",
                prix = 0.0,
                photoUrl = "",
                orderCount = row.totalQuantity,
                disponible = true,
                isValidatedByAdmin = true,
                isChefSpecialty = false
            )
        }

        assertEquals("Bravo", stats[0].nom)
        assertEquals(10, stats[0].orderCount)
        assertEquals("Alpha", stats[1].nom)
        assertEquals(5, stats[1].orderCount)
    }

    private data class FakeDish(val id: String, val nom: String)
}
