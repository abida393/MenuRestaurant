package com.savoria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChefOrderDao {

    @Query("SELECT * FROM chef_orders WHERE status IN ('PENDING', 'PREPARING') ORDER BY timestamp ASC")
    fun getActiveOrders(): Flow<List<ChefOrder>>

    @Query("SELECT COUNT(*) FROM chef_orders")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: ChefOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<ChefOrder>)

    @Update
    suspend fun update(order: ChefOrder)

    @Query("UPDATE chef_orders SET status = :status WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, status: ChefOrderStatus)

    @Query("SELECT * FROM chef_orders WHERE orderId = :parentOrderId")
    suspend fun getByParentOrder(parentOrderId: String): List<ChefOrder>

    @Query("SELECT * FROM chef_orders ORDER BY timestamp ASC")
    fun getAllChefOrders(): Flow<List<ChefOrder>>

    @Query("UPDATE chef_orders SET status = :status WHERE orderId = :parentOrderId")
    suspend fun updateStatusByParentOrder(parentOrderId: String, status: ChefOrderStatus)

    @Query("UPDATE chef_orders SET status = 'CANCELLED' WHERE orderId = :parentOrderId")
    suspend fun cancelByParentOrder(parentOrderId: String)
}
