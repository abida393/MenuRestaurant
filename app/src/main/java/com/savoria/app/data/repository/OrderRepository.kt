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
    val servedOrdersCount: Int = 0,
    val pendingDishesCount: Int = 0
)

data class KitchenOrderCard(
    val order: OrderWithItems,
    val itemLines: List<String>
)

sealed class KitchenListItem {
    data class SectionHeader(val title: String) : KitchenListItem()
    data class ActiveOrder(val card: KitchenOrderCard) : KitchenListItem()
    data class ArchivedOrder(val card: KitchenOrderCard) : KitchenListItem()
}

class OrderRepository(
    private val chefOrderDao: ChefOrderDao,
    private val orderDao: OrderDao,
    private val cartRepository: CartRepository
) {
    val activeKitchenOrders: Flow<List<OrderWithItems>> =
        orderDao.getActiveKitchenOrdersWithItems()

    val archivedKitchenOrders: Flow<List<OrderWithItems>> =
        orderDao.getArchivedKitchenOrdersWithItems()

    val chefOrderLines: Flow<List<ChefOrder>> = chefOrderDao.getAllChefOrders()

    val kitchenListItems: Flow<List<KitchenListItem>> = combine(
        activeKitchenOrders,
        archivedKitchenOrders,
        chefOrderLines
    ) { active, archived, lines ->
        buildList {
            if (active.isNotEmpty()) {
                add(KitchenListItem.SectionHeader("Commandes en cours"))
                active.forEach { order ->
                    add(KitchenListItem.ActiveOrder(toKitchenCard(order, lines)))
                }
            }
            if (archived.isNotEmpty()) {
                add(KitchenListItem.SectionHeader("Commandes servies"))
                archived.forEach { order ->
                    add(KitchenListItem.ArchivedOrder(toKitchenCard(order, lines)))
                }
            }
        }
    }

    val kitchenStats: Flow<Int> = orderDao.countCompletedOrders()

    suspend fun startPreparation(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.EN_PREPARATION)
        chefOrderDao.updateStatusByParentOrder(orderId, ChefOrderStatus.PREPARING)
    }

    suspend fun markReady(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.PRET)
        chefOrderDao.updateStatusByParentOrder(orderId, ChefOrderStatus.READY)
    }

    suspend fun sendExcuse(orderId: String, excuseMessage: String) {
        val order = orderDao.getOrderById(orderId) ?: return
        orderDao.updateExcuse(orderId, excuseMessage)
        orderDao.updateOrderStatus(orderId, OrderStatus.ANNULEE)
        chefOrderDao.cancelByParentOrder(orderId)
        val sessionId = order.clientSessionId
        if (sessionId.isNotBlank()) {
            cartRepository.clear(sessionId)
        }
    }

    private fun toKitchenCard(
        orderWithItems: OrderWithItems,
        lines: List<ChefOrder>
    ): KitchenOrderCard {
        val descriptions = lines
            .filter { it.orderId == orderWithItems.order.id }
            .map { line -> "• ${line.quantity}× ${line.dishName}" }
            .ifEmpty {
                orderWithItems.items.map { item -> "• ${item.quantite}× article" }
            }
        return KitchenOrderCard(orderWithItems, descriptions)
    }
}
