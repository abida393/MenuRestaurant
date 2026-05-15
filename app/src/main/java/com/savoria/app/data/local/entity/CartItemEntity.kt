package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dishId: String,
    val nom: String,
    val prix: Double,
    val quantite: Int,
    val sessionId: String
)
