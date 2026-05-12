package com.savoria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.savoria.app.data.local.entity.Dish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes")
    fun getAllDishes(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :dishId")
    suspend fun getDishById(dishId: String): Dish?

    @Query("SELECT * FROM dishes WHERE categoryId = :categoryId")
    suspend fun getDishesByCategory(categoryId: String): List<Dish>

    @Query("SELECT * FROM dishes WHERE isFavorite = 1")
    fun getFavoriteDishes(): Flow<List<Dish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish): Unit

    @Update
    suspend fun updateDish(dish: Dish): Unit

    @Delete
    suspend fun deleteDish(dish: Dish): Unit
}
