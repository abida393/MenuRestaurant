package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** Commande cuisine (une ligne = un plat commandé). */
@Entity(tableName = "chef_orders")
data class ChefOrder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dishId: String,
    val dishName: String,
    val quantity: Int,
    val price: Double,
    val status: ChefOrderStatus = ChefOrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChefOrderStatus {
    PENDING, PREPARING, READY
}
