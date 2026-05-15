package com.savoria.app.data.local

import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus

object OrderSeeder {

    /**
     * Insère 3 commandes fictives pour tester l'écran Chef.
     * N'insère rien si des commandes existent déjà.
     */
    suspend fun seedSampleChefOrders(database: SavoriaDatabase) {
        val orderDao = database.chefOrderDao()
        if (orderDao.count() > 0) return

        val dishes = database.dishDao().getAllDishesOnce()
        if (dishes.isEmpty()) return

        val now = System.currentTimeMillis()
        val samples = listOf(
            dishes.getOrNull(0),
            dishes.getOrNull(1),
            dishes.getOrNull(2) ?: dishes.first()
        ).filterNotNull()

        val orders = samples.mapIndexed { index, dish ->
            ChefOrder(
                dishId = dish.id,
                dishName = dish.nom,
                quantity = index + 1,
                price = dish.prix,
                status = when (index) {
                    0 -> ChefOrderStatus.PENDING
                    1 -> ChefOrderStatus.PREPARING
                    else -> ChefOrderStatus.PENDING
                },
                timestamp = now - (index * 60_000L)
            )
        }
        orderDao.insertAll(orders)
    }
}
