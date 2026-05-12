package com.savoria.app.ui.admin.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.savoria.app.R
import com.savoria.app.data.local.entity.Category
import com.savoria.app.ui.SharedDishViewModel
import kotlinx.coroutines.launch

class GestionCategoriesFragment : Fragment() {
    private lateinit var viewModel: SharedDishViewModel
    private lateinit var container: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_gestion_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.container = view.findViewById(R.id.ll_category_list)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { cats -> populateCategories(cats) }
        }
    }

    private fun populateCategories(categories: List<Category>) {
        container.removeAllViews()
        for (cat in categories) {
            val tv = android.widget.TextView(context).apply {
                text = cat.nom
                textSize = 15f
                setPadding(24, 20, 24, 20)
                setTextColor(resources.getColor(android.R.color.black, null))
            }
            container.addView(tv)
            val div = android.view.View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                setBackgroundColor(0xFFEEEEEE.toInt())
            }
            container.addView(div)
        }
    }
}
