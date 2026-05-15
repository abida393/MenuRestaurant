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

    // Récupère tout pour l'ADMIN (trié par nom)
    @Query("SELECT * FROM dishes ORDER BY nom ASC")
    fun getAllDishes(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes ORDER BY nom ASC")
    suspend fun getAllDishesOnce(): List<Dish>

    // Plats visibles côté client (validés par l'admin)
    @Query(
        """
        SELECT * FROM dishes 
        WHERE disponible = 1 AND isValidatedByAdmin = 1 
        ORDER BY nom ASC
        """
    )
    fun getAvailableDishes(): Flow<List<Dish>>

    @Query("SELECT COUNT(*) FROM dishes WHERE isValidatedByAdmin = 0")
    fun countPendingValidation(): Flow<Int>

    @Query("SELECT * FROM dishes ORDER BY nom ASC")
    fun getAllDishesForChef(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :dishId")
    suspend fun getDishById(dishId: String): Dish?

    @Query("SELECT * FROM dishes WHERE categoryId = :categoryId AND disponible = 1")
    fun getDishesByCategory(categoryId: String): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE isFavorite = 1")
    fun getFavoriteDishes(): Flow<List<Dish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)

    @Update
    suspend fun updateDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

    // Utile pour l'Admin : activer/désactiver un plat rapidement
    @Query("UPDATE dishes SET disponible = :isAvailable WHERE id = :dishId")
    suspend fun updateAvailability(dishId: String, isAvailable: Boolean)
}