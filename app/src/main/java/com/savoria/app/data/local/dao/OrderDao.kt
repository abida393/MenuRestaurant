package com.savoria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderItem
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>): Unit

    @Update
    suspend fun updateOrder(order: OrderEntity): Unit

    @Delete
    suspend fun deleteOrder(order: OrderEntity): Unit

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Transaction
    @Query("SELECT * FROM orders ORDER BY creeLe DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE statut IN ('RECUE','EN_CUISINE') ORDER BY creeLe ASC")
    fun getPendingOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?
}
