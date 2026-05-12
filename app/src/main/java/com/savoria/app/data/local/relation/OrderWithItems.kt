package com.savoria.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderItem

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)
