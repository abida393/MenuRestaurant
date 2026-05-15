package com.savoria.app.ui.client.suivi

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.savoria.app.data.local.entity.OrderStatus
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.relation.OrderWithItems
import com.savoria.app.ui.client.suivi.SuiviViewModel.Companion.labelFor
import com.savoria.app.ui.client.suivi.SuiviViewModel.Companion.progressFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SuiviOrderAdapter :
    ListAdapter<OrderWithItems, SuiviOrderAdapter.Holder>(DIFF) {

    private val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE)

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvId: TextView = view.findViewById(R.id.tv_suivi_order_id)
        private val tvMode: TextView = view.findViewById(R.id.tv_suivi_mode)
        private val tvStatus: TextView = view.findViewById(R.id.tv_suivi_status_label)
        private val progress: ProgressBar = view.findViewById(R.id.progress_suivi)
        private val tvItems: TextView = view.findViewById(R.id.tv_suivi_items)

        fun bind(item: OrderWithItems) {
            val order = item.order
            tvId.text = "Commande #${order.id.take(6).uppercase()} • ${timeFormat.format(Date(order.creeLe))}"
            tvMode.text = when (order.consommationMode) {
                ConsumptionMode.SUR_PLACE -> "SUR PLACE"
                ConsumptionMode.EMPORTER -> "À EMPORTER"
            }
            tvStatus.text = labelFor(order.statut)
            progress.progress = progressFor(order.statut)
            val progressColor = when (order.statut) {
                OrderStatus.PRET, OrderStatus.SERVI -> Color.parseColor("#2E7D32")
                else -> Color.parseColor("#A02020")
            }
            progress.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)
            tvStatus.setTextColor(progressColor)
            tvItems.text = if (item.items.isEmpty()) {
                "Total : ${String.format(Locale.FRANCE, "%.2f €", order.total)}"
            } else {
                item.items.joinToString("\n") { line ->
                    "• ${line.quantite}× article"
                } + "\nTotal : ${String.format(Locale.FRANCE, "%.2f €", order.total)}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suivi_order, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OrderWithItems>() {
            override fun areItemsTheSame(a: OrderWithItems, b: OrderWithItems) =
                a.order.id == b.order.id

            override fun areContentsTheSame(a: OrderWithItems, b: OrderWithItems) =
                a == b
        }
    }
}
