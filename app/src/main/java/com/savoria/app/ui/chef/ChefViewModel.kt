package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.repository.ChefKitchenStats
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.data.repository.KitchenListItem
import com.savoria.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository: OrderRepository =
        (application as SavoriaApplication).orderRepository

    private val dishRepository: DishRepository =
        (application as SavoriaApplication).dishRepository

    val dashboardStats: StateFlow<ChefKitchenStats> = combine(
        orderRepository.kitchenStats,
        dishRepository.pendingValidationCount
    ) { servedCount, pendingDishes ->
        ChefKitchenStats(
            servedOrdersCount = servedCount,
            pendingDishesCount = pendingDishes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChefKitchenStats())

    val pendingValidationCount: StateFlow<Int> = dishRepository.pendingValidationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val orderListItems: StateFlow<List<KitchenListItem>> = orderRepository.kitchenListItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun startPreparation(orderId: String) {
        viewModelScope.launch { orderRepository.startPreparation(orderId) }
    }

    fun markReady(orderId: String) {
        viewModelScope.launch { orderRepository.markReady(orderId) }
    }

    fun sendExcuse(orderId: String, excuse: String) {
        viewModelScope.launch { orderRepository.sendExcuse(orderId, excuse) }
    }
}
