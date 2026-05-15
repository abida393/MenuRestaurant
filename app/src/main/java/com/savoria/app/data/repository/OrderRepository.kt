package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.ChefOrderDao
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ChefKitchenStats(
    val pendingCount: Int = 0,
    val completedOrdersCount: Int = 0
)

data class KitchenOrderCard(
    val order: OrderWithItems,
    val itemLines: List<String>
)

class OrderRepository(
    private val chefOrderDao: ChefOrderDao,
    private val orderDao: OrderDao
) {
    val activeKitchenOrders: Flow<List<OrderWithItems>> =
        orderDao.getActiveKitchenOrdersWithItems()

    val chefOrderLines: Flow<List<ChefOrder>> = chefOrderDao.getAllChefOrders()

    val kitchenOrderCards: Flow<List<KitchenOrderCard>> = combine(
        activeKitchenOrders,
        chefOrderLines
    ) { orders, lines ->
        val linesByOrder = lines.groupBy { it.orderId }
        orders.map { orderWithItems ->
            val descriptions = linesByOrder[orderWithItems.order.id]?.map { line ->
                "• ${line.quantity}× ${line.dishName}"
            } ?: orderWithItems.items.map { item ->
                "• ${item.quantite}× article"
            }
            KitchenOrderCard(orderWithItems, descriptions)
        }
    }

    val kitchenStats: Flow<ChefKitchenStats> = combine(
        orderDao.countWaitingOrders(),
        orderDao.countCompletedOrders()
    ) { pending, completed ->
        ChefKitchenStats(pendingCount = pending, completedOrdersCount = completed)
    }

    suspend fun startPreparation(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.EN_PREPARATION)
        chefOrderDao.updateStatusByParentOrder(orderId, ChefOrderStatus.PREPARING)
    }

    suspend fun markReady(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.PRET)
        chefOrderDao.updateStatusByParentOrder(orderId, ChefOrderStatus.READY)
    }

    suspend fun sendExcuse(orderId: String, excuseMessage: String) {
        orderDao.updateExcuse(orderId, excuseMessage)
    }
}
