package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.ChefOrderDao
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderItem
import com.savoria.app.data.local.entity.OrderItemStatus
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.Flow

class ClientOrderRepository(
    private val orderDao: OrderDao,
    private val chefOrderDao: ChefOrderDao
) {
    fun activeOrdersForSession(sessionId: String): Flow<List<OrderWithItems>> =
        orderDao.getActiveOrdersForSession(sessionId)

    suspend fun placeOrder(
        sessionId: String,
        mode: ConsumptionMode,
        items: List<CartLine>,
        subtotal: Double,
        tax: Double
    ): OrderEntity {
        val order = OrderEntity(
            clientSessionId = sessionId,
            consommationMode = mode,
            statut = OrderStatus.EN_ATTENTE,
            total = subtotal + tax
        )
        orderDao.insertOrder(order)
        orderDao.insertOrderItems(
            items.map { line ->
                OrderItem(
                    orderId = order.id,
                    dishId = line.dishId,
                    quantite = line.quantity,
                    instructions = "",
                    statutItem = OrderItemStatus.EN_ATTENTE
                )
            }
        )
        chefOrderDao.insertAll(
            items.map { line ->
                ChefOrder(
                    orderId = order.id,
                    dishId = line.dishId,
                    dishName = line.nom,
                    quantity = line.quantity,
                    price = line.prix
                )
            }
        )
        return order
    }
}

data class CartLine(
    val dishId: String,
    val nom: String,
    val prix: Double,
    val quantity: Int
)
