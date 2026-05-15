package com.savoria.app.ui.client.suivi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.ClientSessionManager
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SuiviViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionId = ClientSessionManager.getSessionId(application)
    private val repository = (application as SavoriaApplication).clientOrderRepository

    val activeOrders: StateFlow<List<OrderWithItems>> = repository
        .activeOrdersForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun progressFor(status: OrderStatus): Int = when (status) {
            OrderStatus.EN_ATTENTE -> 25
            OrderStatus.EN_PREPARATION -> 60
            OrderStatus.PRET -> 100
            OrderStatus.SERVI -> 100
        }

        fun labelFor(status: OrderStatus): String = when (status) {
            OrderStatus.EN_ATTENTE -> "En attente — le chef prépare votre commande"
            OrderStatus.EN_PREPARATION -> "En cuisine 👨‍🍳"
            OrderStatus.PRET -> "Prêt ✅ — récupérez votre commande"
            OrderStatus.SERVI -> "Servie"
        }
    }
}
