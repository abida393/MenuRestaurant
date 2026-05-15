package com.savoria.app.ui.admin.plats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.databinding.ItemDishAdminBinding

class DishAdminAdapter(
    private val onEdit: (Dish) -> Unit,
    private val onDelete: (Dish) -> Unit,
    private val onAvailabilityChanged: (Dish, Boolean) -> Unit
) : ListAdapter<Dish, DishAdminAdapter.DishViewHolder>(DIFF_CALLBACK) {

    inner class DishViewHolder(private val binding: ItemDishAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: Dish) {
            binding.textName.text = dish.nom
            binding.textPrice.text = "%.2f €".format(dish.prix)
            binding.textCategory.text = dish.categoryId ?: "Sans catégorie"
            binding.textAvailability.text =
                if (dish.disponible) "✅ Disponible" else "❌ Indisponible"

            binding.badgeChefSpecialty.visibility =
                if (dish.isChefSpecial) View.VISIBLE else View.GONE

            binding.switchAvailable.setOnCheckedChangeListener(null)
            binding.switchAvailable.isChecked = dish.disponible
            binding.switchAvailable.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != dish.disponible) {
                    onAvailabilityChanged(dish, isChecked)
                }
            }

            binding.btnEdit.setOnClickListener { onEdit(dish) }
            binding.btnDelete.setOnClickListener { onDelete(dish) }
            binding.root.alpha = if (dish.disponible) 1.0f else 0.6f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = ItemDishAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Dish>() {
            override fun areItemsTheSame(old: Dish, new: Dish) = old.id == new.id
            override fun areContentsTheSame(old: Dish, new: Dish) = old == new
        }
    }
}
