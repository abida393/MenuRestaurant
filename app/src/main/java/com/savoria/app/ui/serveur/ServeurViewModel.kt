package com.savoria.app.ui.serveur

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.data.local.dao.ChefOrderDao
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServeurOrderCard(
    val order: OrderWithItems,
    val itemLines: List<String>
)

class ServeurViewModel(
    private val orderDao: OrderDao,
    private val chefOrderDao: ChefOrderDao
) : ViewModel() {

    val readyOrders: StateFlow<List<ServeurOrderCard>> = combine(
        orderDao.getReadyToServeOrdersWithItems(),
        chefOrderDao.getAllChefOrders()
    ) { orders, chefLines ->
        val linesByOrder = chefLines.groupBy { it.orderId }
        orders.map { orderWithItems ->
            val lines = linesByOrder[orderWithItems.order.id]?.map { line ->
                "• ${line.quantity}× ${line.dishName}"
            } ?: orderWithItems.items.map { item ->
                "• ${item.quantite}× article"
            }
            ServeurOrderCard(orderWithItems, lines)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markServed(orderId: String) = viewModelScope.launch {
        orderDao.updateOrderStatus(orderId, OrderStatus.SERVI)
    }
}
