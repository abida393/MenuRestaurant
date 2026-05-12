package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChefOrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SavoriaDatabase.getDatabase(
        application, (application as SavoriaApplication).applicationScope
    )

    private val _pendingOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val pendingOrders: StateFlow<List<OrderWithItems>> = _pendingOrders.asStateFlow()

    init {
        viewModelScope.launch {
            db.orderDao().getPendingOrdersWithItems().collect {
                _pendingOrders.value = it
            }
        }
    }

    fun advanceStatus(order: OrderEntity) {
        viewModelScope.launch {
            val next = when (order.statut) {
                OrderStatus.RECUE       -> OrderStatus.EN_CUISINE
                OrderStatus.EN_CUISINE  -> OrderStatus.PRETE
                OrderStatus.PRETE       -> OrderStatus.SERVIE
                OrderStatus.SERVIE      -> OrderStatus.SERVIE
            }
            db.orderDao().updateOrder(order.copy(statut = next))
        }
    }
}
