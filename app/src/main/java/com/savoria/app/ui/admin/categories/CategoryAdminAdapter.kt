package com.savoria.app.ui.admin.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.Category

class CategoryAdminAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onCategoryLongClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdminAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(category: Category) {
            text1.text = category.nom
            text2.text = itemView.context.getString(
                R.string.category_list_meta,
                category.ordreAffichage,
                category.id
            )
            itemView.setOnClickListener { onCategoryClick(category) }
            itemView.setOnLongClickListener {
                onCategoryLongClick(category)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Category, newItem: Category) =
                oldItem == newItem
        }
    }
}
