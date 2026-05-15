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
    indices = [Index("serveurId"), Index("tableId"), Index("clientSessionId")]
)
data class OrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val serveurId: String? = null,
    val tableId: String? = null,
    val statut: OrderStatus = OrderStatus.EN_ATTENTE,
    val consommationMode: ConsumptionMode = ConsumptionMode.SUR_PLACE,
    val clientSessionId: String,
    val total: Double,
    val creeLe: Long = System.currentTimeMillis(),
    val excuseMessage: String? = null
)
