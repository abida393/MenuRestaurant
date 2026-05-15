package com.savoria.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.Category
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedDishViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DishRepository
    
    private val _allDishes = MutableStateFlow<List<Dish>>(emptyList())
    val allDishes: StateFlow<List<Dish>> = _allDishes.asStateFlow()

    private val _favoriteDishes = MutableStateFlow<List<Dish>>(emptyList())
    val favoriteDishes: StateFlow<List<Dish>> = _favoriteDishes.asStateFlow()

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    init {
        val applicationScope = (application as SavoriaApplication).applicationScope
        val dishDao = SavoriaDatabase.getDatabase(application, applicationScope).dishDao()
        repository = DishRepository(dishDao)
        loadAllDishes()
        loadFavoriteDishes()
        loadAllCategories()
    }

    fun loadAllDishes() {
        viewModelScope.launch {
            repository.allDishes.collect { dishes ->
                _allDishes.value = dishes
            }
        }
    }

    fun loadFavoriteDishes() {
        viewModelScope.launch {
            repository.favoriteDishes.collect { dishes ->
                _favoriteDishes.value = dishes
            }
        }
    }

    fun loadAllCategories() {
        viewModelScope.launch {
            val categoryDao = SavoriaDatabase.getDatabase(
                getApplication(), (getApplication() as SavoriaApplication).applicationScope
            ).categoryDao()
            categoryDao.getAllCategories().collect { cats -> _allCategories.value = cats }
        }
    }

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
