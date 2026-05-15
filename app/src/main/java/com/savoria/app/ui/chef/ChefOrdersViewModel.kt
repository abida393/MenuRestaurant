package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefOrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository: OrderRepository =
        (application as SavoriaApplication).orderRepository

    val orders: StateFlow<List<ChefOrder>> = orderRepository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val db = (application as SavoriaApplication).database
            com.savoria.app.data.local.OrderSeeder.seedSampleChefOrders(db)
        }
    }

    fun advanceStatus(order: ChefOrder) {
        viewModelScope.launch {
            orderRepository.advanceStatus(order)
        }
    }
}
