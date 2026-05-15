package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.repository.KitchenOrderCard
import com.savoria.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefOrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository: OrderRepository =
        (application as SavoriaApplication).orderRepository

    val orders: StateFlow<List<KitchenOrderCard>> = orderRepository.kitchenOrderCards
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
