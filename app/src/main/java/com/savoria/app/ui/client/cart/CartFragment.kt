package com.savoria.app.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.savoria.app.R
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartContainer: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var btnOrder: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        cartContainer = view.findViewById(R.id.ll_cart_items)
        tvTotal = view.findViewById(R.id.tv_cart_total)
        btnOrder = view.findViewById(R.id.btn_order)

        btnOrder.setOnClickListener {
            cartViewModel.placeOrder()
            Toast.makeText(context, "Commande placée avec succès !", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItems.collect { items ->
                populateCart(items)
                tvTotal.text = "Total: ${String.format("%.2f €", cartViewModel.total)}"
            }
        }
    }

    private fun populateCart(items: List<CartItem>) {
        if (!::cartContainer.isInitialized) return
        cartContainer.removeAllViews()
        val inflater = LayoutInflater.from(context)

        if (items.isEmpty()) {
            val emptyView = TextView(context).apply {
                text = "Votre panier est vide"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(24, 24, 24, 24) }
            }
            cartContainer.addView(emptyView)
            return
        }

        for (cartItem in items) {
            val item = inflater.inflate(R.layout.item_cart_dish, cartContainer, false)

            item.findViewById<TextView>(R.id.tv_cart_dish_name).text = cartItem.dish.nom
            item.findViewById<TextView>(R.id.tv_cart_dish_price).text = cartItem.dish.prixFormat
            item.findViewById<TextView>(R.id.tv_cart_dish_quantity).text = "Qty: ${cartItem.quantity}"

            item.findViewById<TextView>(R.id.btn_remove_cart_item).setOnClickListener {
                cartViewModel.removeFromCart(cartItem.dish)
                Toast.makeText(context, "${cartItem.dish.nom} retiré du panier", Toast.LENGTH_SHORT).show()
            }

            cartContainer.addView(item)
        }
    }
}
