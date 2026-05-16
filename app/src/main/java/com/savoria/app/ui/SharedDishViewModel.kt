package com.savoria.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.Category
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.relation.OrderWithItems
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.stateInAsListUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SharedDishViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SavoriaApplication
    private val repository: DishRepository = app.dishRepository

    val allDishesState: StateFlow<UiState<List<Dish>>> =
        repository.allDishes.stateInAsListUiState(viewModelScope)

    val favoriteDishesState: StateFlow<UiState<List<Dish>>> =
        repository.favoriteDishes.stateInAsListUiState(viewModelScope)

    val allCategoriesState: StateFlow<UiState<List<Category>>> =
        app.database.categoryDao().getAllCategories().stateInAsListUiState(viewModelScope)

    /** Raw list for combine / one-shot reads after load. */
    val allDishes: StateFlow<List<Dish>> = repository.allDishes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allOrders: StateFlow<List<OrderWithItems>> = app.database.orderDao()
        .getAllOrdersWithItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavorite(dish: Dish) {
        viewModelScope.launch {
            repository.toggleFavorite(dish)
        }
    }

    fun deleteDish(dish: Dish) {
        viewModelScope.launch {
            repository.deleteDish(dish)
        }
    }

    fun insertDish(dish: Dish) {
        viewModelScope.launch {
            repository.insertDish(dish)
        }
    }
}
