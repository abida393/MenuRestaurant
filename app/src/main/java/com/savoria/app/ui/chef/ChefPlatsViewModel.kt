package com.savoria.app.ui.chef

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.repository.DishRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChefPlatsViewModel(application: Application) : AndroidViewModel(application) {

    private val dishRepository: DishRepository =
        (application as SavoriaApplication).dishRepository

    val dishes: StateFlow<List<Dish>> = dishRepository.chefDishes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
