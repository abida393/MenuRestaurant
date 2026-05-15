package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.ChefOrderDao
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val chefOrderDao: ChefOrderDao) {

    val activeOrders: Flow<List<ChefOrder>> = chefOrderDao.getActiveOrders()

    suspend fun insert(order: ChefOrder) = chefOrderDao.insert(order)

    suspend fun insertAll(orders: List<ChefOrder>) = chefOrderDao.insertAll(orders)

    suspend fun advanceStatus(order: ChefOrder) {
        val next = when (order.status) {
            ChefOrderStatus.PENDING -> ChefOrderStatus.PREPARING
            ChefOrderStatus.PREPARING -> ChefOrderStatus.READY
            ChefOrderStatus.READY -> ChefOrderStatus.READY
        }
        chefOrderDao.updateStatus(order.id, next)
    }
}
