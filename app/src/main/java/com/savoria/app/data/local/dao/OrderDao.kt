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
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.model.DishSalesAggregate
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("UPDATE orders SET statut = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)

    @Query("UPDATE orders SET excuseMessage = :message WHERE id = :orderId")
    suspend fun updateExcuse(orderId: String, message: String)

    @Query("SELECT COUNT(*) FROM orders WHERE statut = 'SERVI'")
    fun countCompletedOrders(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders")
    fun countAllOrders(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY creeLe DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM orders 
        WHERE statut IN ('EN_ATTENTE', 'EN_PREPARATION')
        ORDER BY creeLe ASC
        """
    )
    fun getActiveKitchenOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM orders 
        WHERE statut IN ('PRET', 'SERVI')
        ORDER BY creeLe DESC
        LIMIT 50
        """
    )
    fun getArchivedKitchenOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM orders 
        WHERE statut = 'PRET' 
        ORDER BY creeLe ASC
        """
    )
    fun getReadyToServeOrdersWithItems(): Flow<List<OrderWithItems>>

    @Query("SELECT COUNT(*) FROM orders WHERE statut = 'EN_ATTENTE'")
    fun countWaitingOrders(): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders 
        WHERE creeLe >= :dayStartMillis
        """
    )
    fun todayRevenue(dayStartMillis: Long): Flow<Double>

    @Query(
        """
        SELECT oi.dishId AS dishId, SUM(oi.quantite) AS totalQuantity
        FROM order_items AS oi
        INNER JOIN orders AS o ON o.id = oi.orderId
        INNER JOIN dishes AS d ON d.id = oi.dishId
        WHERE oi.dishId IS NOT NULL
        GROUP BY oi.dishId
        ORDER BY totalQuantity DESC
        """
    )
    fun observeDishSalesAggregates(): Flow<List<DishSalesAggregate>>


    @Transaction
    @Query(
        """
        SELECT * FROM orders 
        WHERE clientSessionId = :sessionId 
        AND statut NOT IN ('SERVI', 'ANNULEE')
        ORDER BY creeLe DESC
        """
    )
    fun getActiveOrdersForSession(sessionId: String): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?
}
