package com.savoria.app.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.ui.common.UiState
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutBottomSheet : BottomSheetDialogFragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    var onConfirmed: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_checkout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMode = view.findViewById<TextView>(R.id.tv_invoice_mode)
        val linesContainer = view.findViewById<LinearLayout>(R.id.ll_invoice_lines)
        val tvSubtotal = view.findViewById<TextView>(R.id.tv_invoice_subtotal)
        val tvTax = view.findViewById<TextView>(R.id.tv_invoice_tax)
        val tvTotal = view.findViewById<TextView>(R.id.tv_invoice_total)

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.consumptionMode.collect { mode ->
                tvMode.text = when (mode) {
                    ConsumptionMode.SUR_PLACE -> "Mode : Sur place"
                    ConsumptionMode.EMPORTER -> "Mode : À emporter"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItemsState.collect { state ->
                val items = (state as? UiState.Success)?.data ?: emptyList()
                linesContainer.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())
                items.forEach { item ->
                    val row = inflater.inflate(android.R.layout.simple_list_item_2, linesContainer, false)
                    row.findViewById<TextView>(android.R.id.text1).text = item.nom
                    row.findViewById<TextView>(android.R.id.text2).text =
                        "${item.quantite} × ${String.format(Locale.FRANCE, "%.2f €", item.prix)}"
                    linesContainer.addView(row)
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

        view.findViewById<View>(R.id.btn_confirm_order).setOnClickListener {
            cartViewModel.placeOrder()
            dismiss()
            onConfirmed?.invoke()
        }
    }

    companion object {
        const val TAG = "CheckoutBottomSheet"
    }
}
