package com.savoria.app.data.local.model

import androidx.room.ColumnInfo

/**
 * Result row for [com.savoria.app.data.local.dao.OrderDao.observeDishSalesAggregates].
 */
data class DishSalesAggregate(
    @ColumnInfo(name = "dishId")
    val dishId: String,
    @ColumnInfo(name = "totalQuantity")
    val totalQuantity: Int
)
