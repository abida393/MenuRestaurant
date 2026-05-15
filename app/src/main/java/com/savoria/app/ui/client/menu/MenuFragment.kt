package com.savoria.app.ui.client.menu

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.util.BadgeUtils
import com.savoria.app.ui.admin.login.LoginActivity
import kotlinx.coroutines.launch
import java.util.Locale

class MenuFragment : Fragment() {

    private lateinit var viewModel: SharedDishViewModel
    private var allDishes: List<Dish> = emptyList()

    private var currentCategoryId: String? = null
    private var currentSearchQuery = ""
    private lateinit var container: LinearLayout
    private lateinit var categoryContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.ll_dish_list)
        categoryContainer = view.findViewById(R.id.ll_category_chips)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        // Admin login button
        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // Promo banner action
        view.findViewById<View>(R.id.btn_promo_details).setOnClickListener {
            val bundle = Bundle().apply {
                putString("title", "Risotto\nSafran\ndes Moissons")
                putString("price", "32,00 €")
                putString("description", "Riz Carnaroli mijoté avec des champignons sauvages et des pistils de safran cueillis à la main.")
                putInt("imageRes", 0) // or placeholder
            }
            findNavController().navigate(R.id.action_menu_to_detail, bundle)
        }

        // Sidebar action
        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }

        // Search bar setup
        val etSearch = view.findViewById<EditText>(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().trim()
                filterAndPopulateDishes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observe ViewModel for Categories
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { categories ->
                populateCategories(categories)
            }
        }

        // Observe ViewModel for Dishes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                allDishes = dishes
                filterAndPopulateDishes()
            }
        }
    }

    private fun populateCategories(categories: List<com.savoria.app.data.local.entity.Category>) {
        categoryContainer.removeAllViews()
        
        // Add "All Dishes" chip
        val allChip = createCategoryChip(null, "Tous les Plats")
        categoryContainer.addView(allChip)
        
        categories.forEach { category ->
            val chip = createCategoryChip(category.id, category.nom)
            categoryContainer.addView(chip)
        }
        
        updateChipSelection()
    }

    private fun createCategoryChip(id: String?, name: String): TextView {
        val tv = TextView(context).apply {
            text = name
            textSize = 12f
            setPadding(40, 20, 40, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }
            setOnClickListener {
                currentCategoryId = id
                updateChipSelection()
                filterAndPopulateDishes()
            }
        }
        tv.tag = id
        return tv
    }

    private fun updateChipSelection() {
        for (i in 0 until categoryContainer.childCount) {
            val child = categoryContainer.getChildAt(i) as? TextView ?: continue
            val isSelected = child.tag == currentCategoryId
            
            if (isSelected) {
                child.setBackgroundResource(R.drawable.bg_stat_card_dark)
                child.setTextColor(0xFFFFFFFF.toInt())
            } else {
                child.setBackgroundResource(R.drawable.bg_btn_grey_pill)
                child.setTextColor(0xFF555555.toInt())
            }
        }
    }

    private fun filterAndPopulateDishes() {
        if (!::container.isInitialized) return
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        val filteredDishes = allDishes.filter { dish ->
            val matchesCategory = currentCategoryId == null || dish.categoryId == currentCategoryId
            val matchesSearch = dish.nom.lowercase(Locale.getDefault()).contains(currentSearchQuery.lowercase(Locale.getDefault())) ||
                                dish.description.lowercase(Locale.getDefault()).contains(currentSearchQuery.lowercase(Locale.getDefault()))
            matchesCategory && matchesSearch
        }

        for (dish in filteredDishes) {
            val item = inflater.inflate(R.layout.item_menu_dish, container, false)

            item.findViewById<TextView>(R.id.tv_title).text = dish.nom
            item.findViewById<TextView>(R.id.tv_category).text = dish.categoryId?.uppercase() ?: ""
            item.findViewById<TextView>(R.id.tv_price).text = dish.prixFormat
            
            val imageResId = resources.getIdentifier(dish.photoUrl, "drawable", requireContext().packageName)
            val imageView = item.findViewById<ImageView>(R.id.iv_dish_image)
            if (imageResId != 0) {
                imageView.setImageResource(imageResId)
            }

            val badge = item.findViewById<TextView>(R.id.tv_badge)
            if (dish.badgeText != null) {
                badge.text = dish.badgeText
                if (BadgeUtils.resForType(dish.badgeType) != 0) {
                    badge.setBackgroundResource(BadgeUtils.resForType(dish.badgeType))
                }
                badge.visibility = View.VISIBLE
            } else {
                badge.visibility = View.GONE
            }

            item.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("dishId", dish.id)
                    putString("title", dish.nom)
                    putString("price", dish.prixFormat)
                    putDouble("prixRaw", dish.prix)
                    putString("description", dish.description)
                    putInt("imageRes", imageResId)
                }
                findNavController().navigate(R.id.action_menu_to_detail, bundle)
            }

            container.addView(item)
        }
    }
}
