package com.savoria.app.ui.client.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderItem
import com.savoria.app.data.local.entity.OrderItemStatus
import com.savoria.app.data.local.entity.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartItem(val dish: Dish, val quantity: Int)

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val total get() = _cartItems.value.sumOf { it.dish.prix * it.quantity }

    fun addToCart(dish: Dish) {
        val current = _cartItems.value.toMutableList()
        val existing = current.indexOfFirst { it.dish.id == dish.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(dish, 1))
        }
        _cartItems.value = current
    }

    fun removeFromCart(dish: Dish) {
        _cartItems.value = _cartItems.value.filter { it.dish.id != dish.id }
    }

    fun clearCart() { _cartItems.value = emptyList() }

    fun placeOrder(tableId: String? = null) {
        val items = _cartItems.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val db = SavoriaDatabase.getDatabase(
                getApplication(), (getApplication() as SavoriaApplication).applicationScope
            )
            val order = OrderEntity(
                serveurId = null,
                tableId = tableId,
                statut = OrderStatus.RECUE,
                total = total,
                creeLe = System.currentTimeMillis()
            )
            db.orderDao().insertOrder(order)
            val orderItems = items.map { cartItem ->
                OrderItem(
                    orderId = order.id,
                    dishId = cartItem.dish.id,
                    quantite = cartItem.quantity,
                    instructions = "",
                    statutItem = OrderItemStatus.EN_ATTENTE
                )
            }
            db.orderDao().insertOrderItems(orderItems)
            clearCart()
        }
    }
}
