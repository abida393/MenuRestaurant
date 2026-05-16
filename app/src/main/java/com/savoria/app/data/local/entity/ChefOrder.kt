package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chef_orders",
    indices = [Index("orderId")]
)
data class ChefOrder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val dishId: String,
    val dishName: String,
    val quantity: Int,
    val price: Double,
    val status: ChefOrderStatus = ChefOrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChefOrderStatus {
    PENDING,
    PREPARING,
    READY,
    CANCELLED;

    fun toOrderStatus(): OrderStatus = when (this) {
        PENDING -> OrderStatus.EN_ATTENTE
        PREPARING -> OrderStatus.EN_PREPARATION
        READY -> OrderStatus.PRET
        CANCELLED -> OrderStatus.ANNULEE
    }
}
