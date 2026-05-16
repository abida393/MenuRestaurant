package com.savoria.app.data.local

import com.savoria.app.data.local.database.SavoriaDatabase
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderStatus
object OrderSeeder {

    suspend fun seedSampleChefOrders(database: SavoriaDatabase) {
        val chefOrderDao = database.chefOrderDao()
        if (chefOrderDao.count() > 0) return

        val dishes = database.dishDao().getAllDishesOnce()
        if (dishes.isEmpty()) return

        val sessionId = "demo-session"
        val now = System.currentTimeMillis()
        val samples = dishes.take(3)

        samples.forEachIndexed { index, dish ->
            val parent = OrderEntity(
                clientSessionId = sessionId,
                consommationMode = if (index % 2 == 0) ConsumptionMode.SUR_PLACE else ConsumptionMode.EMPORTER,
                statut = when (index) {
                    0 -> OrderStatus.EN_ATTENTE
                    1 -> OrderStatus.EN_PREPARATION
                    else -> OrderStatus.EN_ATTENTE
                },
                total = dish.prix * (index + 1)
            )
            database.orderDao().insertOrder(parent)
            chefOrderDao.insert(
                ChefOrder(
                    orderId = parent.id,
                    dishId = dish.id,
                    dishName = dish.nom,
                    quantity = index + 1,
                    price = dish.prix,
                    status = when (index) {
                        0 -> ChefOrderStatus.PENDING
                        1 -> ChefOrderStatus.PREPARING
                        else -> ChefOrderStatus.PENDING
                    },
                    timestamp = now - index * 60_000L
                )
            )
        }
    }
}
