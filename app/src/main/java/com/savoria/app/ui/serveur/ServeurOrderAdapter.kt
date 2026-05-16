package com.savoria.app.ui.serveur

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServeurOrderAdapter(
    private val onMarkServed: (String) -> Unit
) : ListAdapter<ServeurOrderCard, ServeurOrderAdapter.ViewHolder>(DIFF) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderMode: TextView = itemView.findViewById(R.id.tv_order_mode)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tv_order_items)
        private val btnMarkServed: View = itemView.findViewById(R.id.btn_mark_served)

        fun bind(card: ServeurOrderCard) {
            val order = card.order.order
            tvOrderId.text = "#${order.id.take(6).uppercase()}"
            tvOrderTime.text = timeFormat.format(Date(order.creeLe))
            tvOrderMode.text = when (order.consommationMode) {
                ConsumptionMode.SUR_PLACE -> "SUR PLACE"
                ConsumptionMode.EMPORTER -> "EMPORTER"
            }
            tvOrderItems.text = card.itemLines.joinToString("\n")

            btnMarkServed.setOnClickListener { onMarkServed(order.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_serveur_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ServeurOrderCard>() {
            override fun areItemsTheSame(old: ServeurOrderCard, new: ServeurOrderCard) =
                old.order.order.id == new.order.order.id

            override fun areContentsTheSame(old: ServeurOrderCard, new: ServeurOrderCard) =
                old == new
        }
    }
}
