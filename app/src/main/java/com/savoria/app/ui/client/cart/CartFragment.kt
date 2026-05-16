package com.savoria.app.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.savoria.app.R
import com.savoria.app.data.local.entity.CartItemEntity
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import kotlinx.coroutines.launch
import java.util.Locale

class CartFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var cartContainer: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvMode: TextView
    private lateinit var btnOrder: TextView
    private lateinit var tvCartEmpty: TextView
    private lateinit var layoutCartFooter: View
    private lateinit var progressCart: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_cart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartContainer = view.findViewById(R.id.ll_cart_items)
        tvSubtotal = view.findViewById(R.id.tv_cart_subtotal)
        tvTax = view.findViewById(R.id.tv_cart_tax)
        tvTotal = view.findViewById(R.id.tv_cart_total)
        tvMode = view.findViewById(R.id.tv_cart_mode)
        btnOrder = view.findViewById(R.id.btn_order)
        tvCartEmpty = view.findViewById(R.id.tv_cart_empty)
        layoutCartFooter = view.findViewById(R.id.layout_cart_footer)
        progressCart = view.findViewById(R.id.progress_cart)

        btnOrder.setOnClickListener {
            val items = (cartViewModel.cartItemsState.value as? UiState.Success)?.data
            if (items.isNullOrEmpty()) return@setOnClickListener
            CheckoutBottomSheet().apply {
                onConfirmed = { /* handled via orderPlaced flow */ }
            }.show(childFragmentManager, CheckoutBottomSheet.TAG)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItemsState.collect { state ->
                when (state) {
                    UiState.Loading -> {
                        progressCart.bindListLoading(true)
                        tvCartEmpty.visibility = View.GONE
                        layoutCartFooter.visibility = View.GONE
                        cartContainer.removeAllViews()
                        btnOrder.isEnabled = false
                    }
                    UiState.Empty -> {
                        progressCart.bindListLoading(false)
                        populateCart(emptyList())
                    }
                    is UiState.Success -> {
                        progressCart.bindListLoading(false)
                        populateCart(state.data)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.invoice.collect { inv ->
                tvSubtotal.text = String.format(Locale.FRANCE, "%.2f €", inv.subtotal)
                tvTax.text = String.format(Locale.FRANCE, "%.2f €", inv.tax)
                tvTotal.text = String.format(Locale.FRANCE, "%.2f €", inv.total)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.consumptionMode.collect { mode ->
                tvMode.text = when (mode) {
                    ConsumptionMode.SUR_PLACE -> "Mode : Sur place"
                    ConsumptionMode.EMPORTER -> "Mode : À emporter"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.orderPlaced.collect { event ->
                event ?: return@collect
                showSuccessAndReceipt(event)
                cartViewModel.clearOrderPlacedEvent()
                findNavController().navigate(R.id.navigation_suivi)
            }
        }
    }

    private fun populateCart(items: List<CartItemEntity>) {
        val isEmpty = items.isEmpty()
        tvCartEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        layoutCartFooter.visibility = if (isEmpty) View.GONE else View.VISIBLE
        btnOrder.isEnabled = !isEmpty

        cartContainer.removeAllViews()
        if (isEmpty) return

        val inflater = LayoutInflater.from(requireContext())
        for (item in items) {
            val row = inflater.inflate(R.layout.item_cart_dish, cartContainer, false)
            row.findViewById<TextView>(R.id.tv_cart_dish_name).text = item.nom
            row.findViewById<TextView>(R.id.tv_cart_dish_price).text =
                String.format(Locale.FRANCE, "%.2f €", item.prix)
            row.findViewById<TextView>(R.id.tv_cart_dish_quantity).text = "Qté : ${item.quantite}"
            row.findViewById<TextView>(R.id.btn_remove_cart_item).setOnClickListener {
                cartViewModel.removeFromCart(item)
            }
            cartContainer.addView(row)
        }
    }

    private fun showSuccessAndReceipt(event: OrderPlacedEvent) {
        val root = requireView()
        Snackbar.make(root, "✓ Commande confirmée avec succès !", Snackbar.LENGTH_LONG)
            .setBackgroundTint(0xFFA02020.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()

        val lines = event.lines.joinToString("\n") {
            "• ${it.nom} × ${it.quantite}"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reçu numérique")
            .setMessage(
                """
                Commande #${event.order.id.take(6).uppercase()}
                ${if (event.order.consommationMode.name == "SUR_PLACE") "Sur place" else "À emporter"}
                
                $lines
                
                Sous-total : ${String.format(Locale.FRANCE, "%.2f €", event.invoice.subtotal)}
                TVA (10%) : ${String.format(Locale.FRANCE, "%.2f €", event.invoice.tax)}
                Total : ${String.format(Locale.FRANCE, "%.2f €", event.invoice.total)}
                
                Merci pour votre confiance — Savoria
                """.trimIndent()
            )
            .setPositiveButton("Voir le suivi", null)
            .show()
    }
}
