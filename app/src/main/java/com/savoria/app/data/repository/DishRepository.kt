package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.DishDao
import com.savoria.app.data.local.entity.Dish
import kotlinx.coroutines.flow.Flow

class DishRepository(private val dishDao: DishDao) {

    val allDishes: Flow<List<Dish>> = dishDao.getAllDishes()

    val availableDishes: Flow<List<Dish>> = dishDao.getAvailableDishes()

    val chefDishes: Flow<List<Dish>> = dishDao.getAllDishesForChef()

    val pendingValidationCount: Flow<Int> = dishDao.countPendingValidation()

    val favoriteDishes: Flow<List<Dish>> = dishDao.getFavoriteDishes()

    suspend fun insert(dish: Dish) = dishDao.insertDish(dish)

    suspend fun update(dish: Dish) = dishDao.updateDish(dish)

    suspend fun delete(dish: Dish) = dishDao.deleteDish(dish)

    suspend fun insertDish(dish: Dish) = insert(dish)

    suspend fun deleteDish(dish: Dish) = delete(dish)

    suspend fun toggleFavorite(dish: Dish) = update(dish.copy(isFavorite = !dish.isFavorite))

    suspend fun setAvailability(dishId: String, available: Boolean) =
        dishDao.updateAvailability(dishId, available)

    fun getByCategory(categoryId: String) = dishDao.getDishesByCategory(categoryId)
}
