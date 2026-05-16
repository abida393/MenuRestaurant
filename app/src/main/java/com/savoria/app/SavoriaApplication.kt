package com.savoria.app

import android.app.Application
import com.savoria.app.data.local.database.SavoriaDatabase
import com.savoria.app.data.repository.CartRepository
import com.savoria.app.data.repository.ClientOrderRepository
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.data.repository.OrderRepository
import com.savoria.app.notification.OrderExcuseNotifier
import com.savoria.app.notification.OrderReadyNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SavoriaApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // On initialise la DB et le Repo de façon "Lazy" (seulement quand on en a besoin)
    val database by lazy { SavoriaDatabase.getDatabase(this, applicationScope) }
    val dishRepository by lazy { DishRepository(database.dishDao()) }
    val cartRepository by lazy { CartRepository(database.cartDao()) }
    val clientOrderRepository by lazy {
        ClientOrderRepository(database.orderDao(), database.chefOrderDao())
    }
    val orderRepository by lazy {
        OrderRepository(database.chefOrderDao(), database.orderDao())
    }

    private val orderReadyNotifier by lazy {
        OrderReadyNotifier(this, database.orderDao(), applicationScope)
    }

    private val orderExcuseNotifier by lazy {
        OrderExcuseNotifier(this, database.orderDao(), applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        orderReadyNotifier.start()
        orderExcuseNotifier.start()
    }
}