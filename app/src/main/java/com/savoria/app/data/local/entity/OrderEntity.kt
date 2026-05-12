package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["serveurId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("serveurId"), Index("tableId")]
)
data class OrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val serveurId: String?,
    val tableId: String?,
    val statut: OrderStatus,
    val total: Double,
    val creeLe: Long
)
