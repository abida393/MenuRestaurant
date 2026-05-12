package com.savoria.app.ui.chef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.savoria.app.R
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.local.relation.OrderWithItems
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChefOrdersFragment : Fragment() {

    private lateinit var viewModel: ChefOrdersViewModel
    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chef_orders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.container = view.findViewById(R.id.ll_orders_container)
        viewModel = ViewModelProvider(this)[ChefOrdersViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingOrders.collect { orders ->
                populateOrders(orders)
            }
        }
    }

    private fun populateOrders(orders: List<OrderWithItems>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        if (orders.isEmpty()) {
            val tv = TextView(context).apply {
                text = "Aucune commande en attente"
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 60, 0, 0)
            }
            container.addView(tv)
            return
        }

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        for (orderWithItems in orders) {
            val order = orderWithItems.order
            val card = inflater.inflate(R.layout.item_chef_order, container, false)

            card.findViewById<TextView>(R.id.tv_order_id).text = "Commande #${order.id.take(6).uppercase()}"
            card.findViewById<TextView>(R.id.tv_order_time).text = sdf.format(Date(order.creeLe))

            val statusBadge = card.findViewById<TextView>(R.id.tv_order_status)
            statusBadge.text = when (order.statut) {
                OrderStatus.RECUE      -> "REÇUE"
                OrderStatus.EN_CUISINE -> "EN CUISINE"
                OrderStatus.PRETE      -> "PRÊTE"
                OrderStatus.SERVIE     -> "SERVIE"
            }

            val itemsList = card.findViewById<LinearLayout>(R.id.ll_items)
            for (item in orderWithItems.items) {
                val tv = TextView(context).apply {
                    text = "× ${item.quantite}  ${item.dishId?.take(8) ?: "plat"}"
                    textSize = 14f
                    setPadding(0, 4, 0, 4)
                }
                itemsList.addView(tv)
            }

            card.findViewById<TextView>(R.id.btn_advance_status).apply {
                text = when (order.statut) {
                    OrderStatus.RECUE      -> "Démarrer la préparation"
                    OrderStatus.EN_CUISINE -> "Marquer comme prête"
                    OrderStatus.PRETE      -> "Marquer comme servie"
                    OrderStatus.SERVIE     -> "✓ Terminée"
                }
                isEnabled = order.statut != OrderStatus.SERVIE
                setOnClickListener { viewModel.advanceStatus(order) }
            }

            container.addView(card)
        }
    }
}
