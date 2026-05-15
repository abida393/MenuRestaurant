package com.savoria.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalDishes: Int = 0,
    val chefSpecialties: Int = 0,
    val unavailableDishes: Int = 0
)

class AdminViewModel(private val repository: DishRepository) : ViewModel() {

    val allDishes: StateFlow<List<Dish>> = repository.allDishes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = repository.allDishes
        .map { dishes ->
            DashboardStats(
                totalDishes = dishes.size,
                chefSpecialties = dishes.count { it.isChefSpecial },
                unavailableDishes = dishes.count { !it.disponible }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    fun addDish(dish: Dish) = viewModelScope.launch {
        repository.insert(dish)
        _saveMessage.value = "Plat ajouté"
    }

    fun updateDish(dish: Dish) = viewModelScope.launch {
        repository.update(dish)
        _saveMessage.value = "Plat mis à jour"
    }

    fun deleteDish(dish: Dish) = viewModelScope.launch { repository.delete(dish) }

    fun setAvailability(dish: Dish, available: Boolean) = viewModelScope.launch {
        if (dish.disponible != available) {
            repository.update(dish.copy(disponible = available))
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }
}
