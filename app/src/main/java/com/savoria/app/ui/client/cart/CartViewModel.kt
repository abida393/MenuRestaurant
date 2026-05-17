package com.savoria.app.ui.client.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.ClientSessionManager
import com.savoria.app.data.local.entity.CartItemEntity
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.repository.CartLine
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.stateInAsListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartInvoice(
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0
)

data class OrderPlacedEvent(
    val order: OrderEntity,
    val invoice: CartInvoice,
    val lines: List<CartItemEntity>
)

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SavoriaApplication
    private val sessionId = ClientSessionManager.getSessionId(application)
    private val cartItemsFlow = app.cartRepository.observeCart(sessionId)

    val cartItemsState: StateFlow<UiState<List<CartItemEntity>>> =
        cartItemsFlow.stateInAsListUiState(viewModelScope)

    val invoice: StateFlow<CartInvoice> = cartItemsFlow
        .map { items -> buildInvoice(items) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartInvoice())

    private val _consumptionMode = MutableStateFlow(ConsumptionMode.SUR_PLACE)
    val consumptionMode: StateFlow<ConsumptionMode> = _consumptionMode.asStateFlow()

    private val _orderPlaced = MutableStateFlow<OrderPlacedEvent?>(null)
    val orderPlaced: StateFlow<OrderPlacedEvent?> = _orderPlaced.asStateFlow()

    fun setConsumptionMode(mode: ConsumptionMode) {
        _consumptionMode.value = mode
    }

    fun addToCart(dish: Dish, mode: ConsumptionMode) {
        _consumptionMode.value = mode
        viewModelScope.launch {
            app.cartRepository.addItem(sessionId, dish.id, dish.nom, dish.prix)
        }
    }

    fun removeFromCart(item: CartItemEntity) {
        decrementQuantity(item)
    }

    fun forceRemoveFromCart(item: CartItemEntity) {
        viewModelScope.launch { app.cartRepository.removeItem(item) }
    }


    fun incrementQuantity(item: CartItemEntity) {
        viewModelScope.launch {
            app.cartRepository.updateQuantity(item, item.quantite + 1)
        }
    }

    fun decrementQuantity(item: CartItemEntity) {
        viewModelScope.launch {
            app.cartRepository.updateQuantity(item, item.quantite - 1)
        }
    }

    fun clearOrderPlacedEvent() {
        _orderPlaced.value = null
    }

    fun placeOrder() {
        val items = (cartItemsState.value as? UiState.Success)?.data ?: return
        if (items.isEmpty()) return
        viewModelScope.launch {
            val inv = buildInvoice(items)
            val order = app.clientOrderRepository.placeOrder(
                sessionId = sessionId,
                mode = _consumptionMode.value,
                items = items.map {
                    CartLine(it.dishId, it.nom, it.prix, it.quantite)
                },
                subtotal = inv.subtotal,
                tax = inv.tax
            )
            app.cartRepository.clear(sessionId)
            _orderPlaced.value = OrderPlacedEvent(order, inv, items)
        }
    }

    private fun buildInvoice(items: List<CartItemEntity>): CartInvoice {
        val subtotal = items.sumOf { it.prix * it.quantite }
        val tax = subtotal * TAX_RATE
        return CartInvoice(
            subtotal = subtotal,
            tax = tax,
            total = subtotal + tax,
            itemCount = items.sumOf { it.quantite }
        )
    }

    companion object {
        const val TAX_RATE = 0.10
    }
}
