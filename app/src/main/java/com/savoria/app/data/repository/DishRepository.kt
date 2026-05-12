package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.DishDao
import com.savoria.app.data.local.entity.Dish
import kotlinx.coroutines.flow.Flow

class DishRepository(private val dishDao: DishDao) {

    fun getAllDishes(): Flow<List<Dish>> = dishDao.getAllDishes()

    fun getFavoriteDishes(): Flow<List<Dish>> = dishDao.getFavoriteDishes()

    suspend fun toggleFavorite(dish: Dish) {
        val updatedDish = dish.copy(isFavorite = !dish.isFavorite)
        dishDao.updateDish(updatedDish)
    }

    suspend fun insertDish(dish: Dish) {
        dishDao.insertDish(dish)
    }

    suspend fun deleteDish(dish: Dish) {
        dishDao.deleteDish(dish)
    }
}
