package com.savoria.app.ui.chef

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.OrderStatus
import com.savoria.app.data.repository.KitchenListItem
import com.savoria.app.data.repository.KitchenOrderCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val onStartPreparation: (String) -> Unit,
    private val onMarkReady: (String) -> Unit,
    private val onSendExcuse: (String) -> Unit
) : ListAdapter<KitchenListItem, RecyclerView.ViewHolder>(DIFF) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is KitchenListItem.SectionHeader -> VIEW_HEADER
        is KitchenListItem.ActiveOrder -> VIEW_ACTIVE
        is KitchenListItem.ArchivedOrder -> VIEW_ARCHIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_chef_order_header, parent, false)
            )
            else -> OrderViewHolder(
                inflater.inflate(R.layout.item_chef_order, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is KitchenListItem.SectionHeader -> (holder as HeaderViewHolder).bind(item.title)
            is KitchenListItem.ActiveOrder -> (holder as OrderViewHolder).bind(item.card, isArchived = false)
            is KitchenListItem.ArchivedOrder -> (holder as OrderViewHolder).bind(item.card, isArchived = true)
        }
    }

    private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view as TextView
        fun bind(text: String) {
            title.text = text
        }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderMode: TextView = itemView.findViewById(R.id.tv_order_mode)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tv_order_items)
        private val btnStart: MaterialButton = itemView.findViewById(R.id.btn_start_preparation)
        private val btnReady: MaterialButton = itemView.findViewById(R.id.btn_mark_ready)
        private val btnExcuse: MaterialButton = itemView.findViewById(R.id.btn_send_excuse)

        fun bind(cardData: KitchenOrderCard, isArchived: Boolean) {
            val order = cardData.order.order
            tvOrderId.text = "#${order.id.take(6).uppercase()}"
            tvOrderTime.text = timeFormat.format(Date(order.creeLe))
            tvOrderMode.text = when (order.consommationMode) {
                ConsumptionMode.SUR_PLACE -> "SUR PLACE"
                ConsumptionMode.EMPORTER -> "EMPORTER"
            }
            tvOrderItems.text = cardData.itemLines.joinToString("\n")

            val (statusLabel, statusColor) = when (order.statut) {
                OrderStatus.EN_ATTENTE -> "EN ATTENTE" to Color.parseColor("#E53935")
                OrderStatus.EN_PREPARATION -> "EN PRÉPARATION" to Color.parseColor("#F9A825")
                OrderStatus.PRET -> "PRÊT" to Color.parseColor("#2E7D32")
                OrderStatus.SERVI -> "SERVI" to Color.parseColor("#9E9E9E")
                OrderStatus.ANNULEE -> "ANNULÉE" to Color.parseColor("#9E9E9E")
            }
            tvOrderStatus.text = statusLabel
            tvOrderStatus.setBackgroundColor(statusColor)

            card.alpha = if (isArchived) 0.5f else 1f
            card.cardElevation = if (isArchived) 0f else 4f

            val isInProgress = order.statut == OrderStatus.EN_ATTENTE ||
                order.statut == OrderStatus.EN_PREPARATION

            if (isArchived) {
                btnStart.visibility = View.GONE
                btnReady.visibility = View.GONE
                btnExcuse.visibility = View.GONE
            } else {
                btnStart.visibility = if (order.statut == OrderStatus.EN_ATTENTE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                btnReady.visibility = if (order.statut == OrderStatus.EN_PREPARATION) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                btnExcuse.visibility = if (isInProgress) View.VISIBLE else View.GONE

                btnStart.setOnClickListener { onStartPreparation(order.id) }
                btnReady.setOnClickListener { onMarkReady(order.id) }
                btnExcuse.setOnClickListener { onSendExcuse(order.id) }
            }
        }
    }

    companion object {
        private const val VIEW_HEADER = 0
        private const val VIEW_ACTIVE = 1
        private const val VIEW_ARCHIVED = 2

        private val DIFF = object : DiffUtil.ItemCallback<KitchenListItem>() {
            override fun areItemsTheSame(oldItem: KitchenListItem, newItem: KitchenListItem): Boolean {
                return when {
                    oldItem is KitchenListItem.SectionHeader && newItem is KitchenListItem.SectionHeader ->
                        oldItem.title == newItem.title
                    oldItem is KitchenListItem.ActiveOrder && newItem is KitchenListItem.ActiveOrder ->
                        oldItem.card.order.order.id == newItem.card.order.order.id
                    oldItem is KitchenListItem.ArchivedOrder && newItem is KitchenListItem.ArchivedOrder ->
                        oldItem.card.order.order.id == newItem.card.order.order.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: KitchenListItem, newItem: KitchenListItem): Boolean {
                return when {
                    oldItem is KitchenListItem.SectionHeader && newItem is KitchenListItem.SectionHeader ->
                        oldItem.title == newItem.title
                    oldItem is KitchenListItem.ActiveOrder && newItem is KitchenListItem.ActiveOrder ->
                        sameKitchenCard(oldItem.card, newItem.card)
                    oldItem is KitchenListItem.ArchivedOrder && newItem is KitchenListItem.ArchivedOrder ->
                        sameKitchenCard(oldItem.card, newItem.card)
                    else -> false
                }
            }

            private fun sameKitchenCard(a: KitchenOrderCard, b: KitchenOrderCard): Boolean =
                a.order.order.id == b.order.order.id &&
                    a.order.order.statut == b.order.order.statut &&
                    a.itemLines == b.itemLines
        }
    }
}
