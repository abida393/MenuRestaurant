package com.savoria.app.ui.client.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R

class MenuPromoAdapter(
    private val onPromoClick: () -> Unit
) : RecyclerView.Adapter<MenuPromoAdapter.PromoViewHolder>() {

    var showPromo: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    inner class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<View>(R.id.btn_promo_details).setOnClickListener {
                onPromoClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_promo, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) = Unit

    override fun getItemCount(): Int = if (showPromo) 1 else 0
}
