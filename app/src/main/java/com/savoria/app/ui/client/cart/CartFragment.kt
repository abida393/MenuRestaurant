package com.savoria.app.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

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
    private lateinit var cartAdapter: CartAdapter
    private lateinit var recyclerCart: RecyclerView

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

        recyclerCart = view.findViewById(R.id.recycler_cart)
        tvSubtotal = view.findViewById(R.id.tv_cart_subtotal)
        tvTax = view.findViewById(R.id.tv_cart_tax)
        tvTotal = view.findViewById(R.id.tv_cart_total)
        tvMode = view.findViewById(R.id.tv_cart_mode)
        btnOrder = view.findViewById(R.id.btn_order)
        tvCartEmpty = view.findViewById(R.id.tv_cart_empty)
        layoutCartFooter = view.findViewById(R.id.layout_cart_footer)
        progressCart = view.findViewById(R.id.progress_cart)
 
        cartAdapter = CartAdapter(
            onIncrement = { cartViewModel.incrementQuantity(it) },
            onDecrement = { cartViewModel.decrementQuantity(it) },
            onRemoveAll = { confirmDeletion(it) }
        )
        recyclerCart.adapter = cartAdapter

        setupSwipeToDelete()



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
                        cartAdapter.submitList(emptyList())
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
        cartAdapter.submitList(items)
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = cartAdapter.currentList[position]
                confirmDeletion(item)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerCart)
    }

    private fun confirmDeletion(item: CartItemEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer l'article ?")
            .setMessage("Voulez-vous retirer tous les exemplaires de ${item.nom} du panier ?")
            .setNegativeButton("Annuler") { _, _ -> 
                cartAdapter.notifyDataSetChanged() // Reset swipe state
            }
            .setPositiveButton("Supprimer") { _, _ ->
                cartViewModel.forceRemoveFromCart(item)
                Snackbar.make(requireView(), "${item.nom} retiré du panier", Snackbar.LENGTH_SHORT).show()
            }
            .setOnCancelListener {
                cartAdapter.notifyDataSetChanged()
            }
            .show()
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
