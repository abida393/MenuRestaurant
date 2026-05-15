package com.savoria.app

import android.app.Application
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.data.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SavoriaApplication : Application() {
    // On crée un scope pour les opérations de la base de données
    val applicationScope = CoroutineScope(SupervisorJob())

    // On initialise la DB et le Repo de façon "Lazy" (seulement quand on en a besoin)
    val database by lazy { SavoriaDatabase.getDatabase(this, applicationScope) }
    val dishRepository by lazy { DishRepository(database.dishDao()) }
    val orderRepository by lazy {
        OrderRepository(database.chefOrderDao())
    }
}