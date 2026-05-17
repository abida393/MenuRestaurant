package com.savoria.app.data.repository

import com.savoria.app.data.local.dao.CartDao
import com.savoria.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun observeCart(sessionId: String): Flow<List<CartItemEntity>> =
        cartDao.getCartForSession(sessionId)

    suspend fun addItem(sessionId: String, dishId: String, nom: String, prix: Double) {
        val existing = cartDao.findByDish(sessionId, dishId)
        if (existing != null) {
            cartDao.insert(existing.copy(quantite = existing.quantite + 1))
        } else {
            cartDao.insert(
                CartItemEntity(
                    dishId = dishId,
                    nom = nom,
                    prix = prix,
                    quantite = 1,
                    sessionId = sessionId
                )
            )
        }
    }

    suspend fun removeItem(item: CartItemEntity) = cartDao.deleteById(item.id)

    suspend fun updateQuantity(item: CartItemEntity, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteById(item.id)
        } else {
            cartDao.insert(item.copy(quantite = newQuantity))
        }
    }

    suspend fun clear(sessionId: String) = cartDao.clearSession(sessionId)
}
