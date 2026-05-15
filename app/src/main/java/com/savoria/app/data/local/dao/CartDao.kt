package com.savoria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.savoria.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE sessionId = :sessionId ORDER BY nom ASC")
    fun getCartForSession(sessionId: String): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("DELETE FROM cart_items WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: String)

    @Query("SELECT * FROM cart_items WHERE sessionId = :sessionId AND dishId = :dishId LIMIT 1")
    suspend fun findByDish(sessionId: String, dishId: String): CartItemEntity?
}
