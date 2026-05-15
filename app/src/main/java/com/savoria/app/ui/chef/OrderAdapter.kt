package com.savoria.app.ui.chef

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.repository.KitchenOrderCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val onStartPreparation: (String) -> Unit,
    private val onMarkReady: (String) -> Unit,
    private val onSendExcuse: (String) -> Unit
) : ListAdapter<KitchenOrderCard, OrderAdapter.OrderViewHolder>(DIFF) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderMode: TextView = itemView.findViewById(R.id.tv_order_mode)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tv_order_items)
        private val btnStart: TextView = itemView.findViewById(R.id.btn_start_preparation)
        private val btnReady: TextView = itemView.findViewById(R.id.btn_mark_ready)
        private val btnExcuse: TextView = itemView.findViewById(R.id.btn_send_excuse)

        fun bind(card: KitchenOrderCard) {
            val order = card.order.order
            tvOrderId.text = "#${order.id.take(6).uppercase()}"
            tvOrderTime.text = timeFormat.format(Date(order.creeLe))
            tvOrderMode.text = when (order.consommationMode) {
                ConsumptionMode.SUR_PLACE -> "SUR PLACE"
                ConsumptionMode.EMPORTER -> "EMPORTER"
            }
            tvOrderItems.text = card.itemLines.joinToString("\n")

            val (statusLabel, statusColor) = when (order.statut) {
                OrderStatus.EN_ATTENTE -> "EN ATTENTE" to Color.parseColor("#FF6B6B")
                OrderStatus.EN_PREPARATION -> "EN PRÉPARATION" to Color.parseColor("#F9A825")
                OrderStatus.PRET -> "PRÊT" to Color.parseColor("#2E7D32")
                OrderStatus.SERVI -> "SERVI" to Color.parseColor("#9E9E9E")
            }
            tvOrderStatus.text = statusLabel
            tvOrderStatus.setBackgroundColor(statusColor)

            btnStart.visibility = when (order.statut) {
                OrderStatus.EN_ATTENTE -> View.VISIBLE
                else -> View.GONE
            }
            btnReady.visibility = when (order.statut) {
                OrderStatus.EN_PREPARATION -> View.VISIBLE
                else -> View.GONE
            }

            val readyEnabled = order.statut == OrderStatus.EN_PREPARATION
            btnReady.isEnabled = readyEnabled
            btnReady.alpha = if (readyEnabled) 1f else 0.5f

            btnStart.setOnClickListener { onStartPreparation(order.id) }
            btnReady.setOnClickListener { onMarkReady(order.id) }
            btnExcuse.setOnClickListener { onSendExcuse(order.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chef_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<KitchenOrderCard>() {
            override fun areItemsTheSame(oldItem: KitchenOrderCard, newItem: KitchenOrderCard) =
                oldItem.order.order.id == newItem.order.order.id

            override fun areContentsTheSame(oldItem: KitchenOrderCard, newItem: KitchenOrderCard) =
                oldItem == newItem
        }
    }
}
