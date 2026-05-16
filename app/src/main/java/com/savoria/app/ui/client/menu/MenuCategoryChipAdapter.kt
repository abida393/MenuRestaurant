package com.savoria.app.ui.client.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R

data class MenuCategoryChip(
    val categoryId: String?,
    val label: String
)

class MenuCategoryChipAdapter(
    private val onChipSelected: (String?) -> Unit
) : ListAdapter<MenuCategoryChip, MenuCategoryChipAdapter.ChipViewHolder>(DIFF_CALLBACK) {

    var selectedCategoryId: String? = null
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION)
        }

    inner class ChipViewHolder(private val chip: TextView) : RecyclerView.ViewHolder(chip) {
        fun bind(item: MenuCategoryChip, selected: Boolean) {
            chip.text = item.label
            chip.tag = item.categoryId
            chip.setBackgroundResource(
                if (selected) R.drawable.bg_stat_card_dark else R.drawable.bg_btn_grey_pill
            )
            val textColors = ContextCompat.getColorStateList(chip.context, R.color.chip_filter_text)
            if (textColors != null) {
                val state = if (selected) {
                    intArrayOf(android.R.attr.state_checked)
                } else {
                    intArrayOf()
                }
                chip.setTextColor(textColors.getColorForState(state, textColors.defaultColor))
            }
            chip.setOnClickListener {
                selectedCategoryId = item.categoryId
                onChipSelected(item.categoryId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_category_chip, parent, false) as TextView
        return ChipViewHolder(chip)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.categoryId == selectedCategoryId)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SELECTION)) {
            holder.bind(getItem(position), getItem(position).categoryId == selectedCategoryId)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        private const val PAYLOAD_SELECTION = "selection"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MenuCategoryChip>() {
            override fun areItemsTheSame(old: MenuCategoryChip, new: MenuCategoryChip) =
                old.categoryId == new.categoryId

            override fun areContentsTheSame(old: MenuCategoryChip, new: MenuCategoryChip) =
                old == new
        }
    }
}
