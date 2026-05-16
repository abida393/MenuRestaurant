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
import com.savoria.app.data.local.entity.Dish
import java.util.Locale

class ChefDishAdapter : ListAdapter<Dish, ChefDishAdapter.Holder>(DIFF) {

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tv_chef_dish_name)
        private val price: TextView = view.findViewById(R.id.tv_chef_dish_price)
        private val badge: TextView = view.findViewById(R.id.tv_validation_badge)

        fun bind(dish: Dish) {
            name.text = dish.nom
            price.text = dish.prixFormat.ifBlank {
                String.format(Locale.FRANCE, "%.2f €", dish.prix)
            }
            if (dish.isValidatedByAdmin) {
                badge.visibility = View.GONE
            } else {
                badge.visibility = View.VISIBLE
                badge.text = "En attente de validation"
                badge.setBackgroundResource(R.drawable.bg_badge_red)
                badge.setTextColor(Color.parseColor("#C0392B"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chef_dish, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Dish>() {
            override fun areItemsTheSame(a: Dish, b: Dish) = a.id == b.id
            override fun areContentsTheSame(a: Dish, b: Dish) = a == b
        }
    }
}
