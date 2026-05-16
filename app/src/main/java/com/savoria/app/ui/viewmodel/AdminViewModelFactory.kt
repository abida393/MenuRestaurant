package com.savoria.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.savoria.app.SavoriaApplication

class AdminViewModelFactory(
    private val application: SavoriaApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            val db = application.database
            return AdminViewModel(
                application.dishRepository,
                db.categoryDao(),
                db.userDao(),
                db.orderDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
