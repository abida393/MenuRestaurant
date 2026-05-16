package com.savoria.app.ui.client.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.util.BadgeUtils
import com.savoria.app.ui.util.DishImageLoader

class MenuDishAdapter(
    private val onDishClick: (Dish) -> Unit
) : ListAdapter<Dish, MenuDishAdapter.DishViewHolder>(DIFF_CALLBACK) {

    private var categoryNamesById: Map<String, String> = emptyMap()

    fun submitCategoryMap(categoryMap: Map<String, String>) {
        categoryNamesById = categoryMap
        if (itemCount > 0) {
            notifyItemRangeChanged(0, itemCount, PAYLOAD_CATEGORY_NAMES)
        }
    }

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_dish_image)
        private val tvBadge: TextView = itemView.findViewById(R.id.tv_badge)

        fun bind(dish: Dish) {
            tvTitle.text = dish.nom
            tvCategory.text = dish.categoryId?.let { categoryNamesById[it] }.orEmpty()
            tvPrice.text = dish.prixFormat

            DishImageLoader.load(ivImage, dish.photoUrl)

            if (dish.badgeText != null) {
                tvBadge.text = dish.badgeText
                val badgeRes = BadgeUtils.resForType(dish.badgeType)
                if (badgeRes != 0) {
                    tvBadge.setBackgroundResource(badgeRes)
                }
                tvBadge.visibility = View.VISIBLE
            } else {
                tvBadge.visibility = View.GONE
            }

            itemView.setOnClickListener { onDishClick(dish) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: DishViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_CATEGORY_NAMES)) {
            holder.bind(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        private const val PAYLOAD_CATEGORY_NAMES = "category_names"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Dish>() {
            override fun areItemsTheSame(oldItem: Dish, newItem: Dish) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Dish, newItem: Dish) = oldItem == newItem
        }
    }
}
