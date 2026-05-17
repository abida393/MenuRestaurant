package com.savoria.app.ui.client.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.CartItemEntity
import java.util.Locale

class CartAdapter(
    private val onIncrement: (CartItemEntity) -> Unit,
    private val onDecrement: (CartItemEntity) -> Unit,
    private val onRemoveAll: (CartItemEntity) -> Unit
) : ListAdapter<CartItemEntity, CartAdapter.CartViewHolder>(DIFF_CALLBACK) {


    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_cart_dish_name)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_cart_dish_price)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_cart_dish_quantity)
        private val btnIncrement: ImageButton = itemView.findViewById(R.id.btn_increment)
        private val btnDecrement: ImageButton = itemView.findViewById(R.id.btn_decrement)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove_cart_item)


        fun bind(item: CartItemEntity) {
            tvName.text = item.nom
            tvPrice.text = String.format(Locale.FRANCE, "%.2f €", item.prix)
            tvQuantity.text = item.quantite.toString()

            btnIncrement.setOnClickListener { onIncrement(item) }
            btnDecrement.setOnClickListener { onDecrement(item) }
            btnRemove.setOnClickListener { onDecrement(item) } // Per user request: removeFromCart should decrement
            
            itemView.setOnLongClickListener {
                onRemoveAll(item)
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_dish, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartItemEntity>() {
            override fun areItemsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity) =
                oldItem == newItem
        }
    }
}
