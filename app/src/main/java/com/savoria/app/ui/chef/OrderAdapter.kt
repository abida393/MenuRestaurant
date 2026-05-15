package com.savoria.app.ui.chef

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.ChefOrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val onAdvanceStatus: (ChefOrder) -> Unit
) : ListAdapter<ChefOrder, OrderAdapter.OrderViewHolder>(DIFF) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvDishName: TextView = itemView.findViewById(R.id.tv_dish_name)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val btnAdvance: TextView = itemView.findViewById(R.id.btn_advance_status)

        fun bind(order: ChefOrder) {
            tvOrderId.text = "Commande #${order.id.take(6).uppercase()}"
            tvOrderTime.text = timeFormat.format(Date(order.timestamp))
            tvDishName.text = order.dishName
            tvQuantity.text = "Qté : ${order.quantity}"
            tvPrice.text = String.format(Locale.FRANCE, "%.2f €", order.price * order.quantity)

            tvOrderStatus.text = when (order.status) {
                ChefOrderStatus.PENDING -> "EN ATTENTE"
                ChefOrderStatus.PREPARING -> "EN PRÉPARATION"
                ChefOrderStatus.READY -> "PRÊTE"
            }

            btnAdvance.text = when (order.status) {
                ChefOrderStatus.PENDING -> "Lancer la préparation"
                ChefOrderStatus.PREPARING -> "Plat prêt"
                ChefOrderStatus.READY -> "✓ Terminée"
            }
            btnAdvance.isEnabled = order.status != ChefOrderStatus.READY
            btnAdvance.setOnClickListener { onAdvanceStatus(order) }
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
        private val DIFF = object : DiffUtil.ItemCallback<ChefOrder>() {
            override fun areItemsTheSame(oldItem: ChefOrder, newItem: ChefOrder) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChefOrder, newItem: ChefOrder) =
                oldItem == newItem
        }
    }
}
