package com.savoria.app.ui.serveur

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.savoria.app.SavoriaApplication

class ServeurViewModelFactory(
    private val application: SavoriaApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServeurViewModel::class.java)) {
            val db = application.database
            return ServeurViewModel(db.orderDao(), db.chefOrderDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
