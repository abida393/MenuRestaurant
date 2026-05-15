package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.repository.ChefKitchenStats
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChefDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository: OrderRepository =
        (application as SavoriaApplication).orderRepository

    private val dishRepository: DishRepository =
        (application as SavoriaApplication).dishRepository

    val stats: StateFlow<ChefKitchenStats> = orderRepository.kitchenStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChefKitchenStats())

    val pendingValidationCount = dishRepository.pendingValidationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
