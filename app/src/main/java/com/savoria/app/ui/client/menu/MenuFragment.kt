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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.util.BadgeUtils
import kotlinx.coroutines.launch
import java.util.Locale

class MenuFragment : Fragment() {

    private lateinit var viewModel: SharedDishViewModel
    private var allDishes: List<Dish> = emptyList()
    private var currentCategory = "All Dishes"
    private var specialtyOnly = false
    private var currentSearchQuery = ""
    private lateinit var container: LinearLayout
    private lateinit var categoryContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.ll_dish_list)
        categoryContainer = view.findViewById(R.id.ll_category_chips)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        arguments?.getString("categoryFilter")?.takeIf { it.isNotBlank() }?.let {
            currentCategory = it
        }
        specialtyOnly = arguments?.getBoolean("specialtyOnly") == true

        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }

        view.findViewById<View>(R.id.btn_promo_details).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val clientDishes = allDishes.filter { it.disponible && it.isValidatedByAdmin }
                val promo = clientDishes.firstOrNull { it.isChefSpecial }
                    ?: clientDishes.firstOrNull { it.badgeText != null }
                    ?: clientDishes.firstOrNull()
                promo?.let { navigateToDetail(it) }
            }
        }

        setupChips(view)
        setupSearch(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                allDishes = dishes
                applyNavFilterSelection(view)
                filterAndPopulateDishes()
            }
        }
    }

    private fun setupChips(view: View) {
        val chipAll = view.findViewById<TextView>(R.id.chip_all)
        val chipMains = view.findViewById<TextView>(R.id.chip_mains)
        val chipStarters = view.findViewById<TextView>(R.id.chip_starters)
        val chipDesserts = view.findViewById<TextView>(R.id.chip_desserts)
        val chipSeafood = view.findViewById<TextView>(R.id.chip_seafood)

        val chips = listOf(chipAll, chipMains, chipStarters, chipDesserts, chipSeafood)
        val categoryMap = mapOf(
            chipAll to "All Dishes",
            chipMains to "Mains",
            chipStarters to "Starters",
            chipDesserts to "Desserts",
            chipSeafood to "Seafood"
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { c ->
                    c.setBackgroundResource(R.drawable.bg_btn_grey_pill)
                    c.setTextColor(0xFF555555.toInt())
                }
                chip.setBackgroundResource(R.drawable.bg_stat_card_dark)
                chip.setTextColor(0xFFFFFFFF.toInt())
                currentCategory = categoryMap[chip] ?: "All Dishes"
                specialtyOnly = false
                filterAndPopulateDishes()
            }
        }
    }

    private fun applyNavFilterSelection(view: View) {
        if (specialtyOnly) {
            currentCategory = "All Dishes"
            return
        }
        val chipId = when (currentCategory) {
            "Mains" -> R.id.chip_mains
            "Starters" -> R.id.chip_starters
            "Desserts" -> R.id.chip_desserts
            "Seafood" -> R.id.chip_seafood
            else -> return
        }
        view.findViewById<TextView>(chipId)?.performClick()
    }

    private fun setupSearch(view: View) {
        view.findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().trim()
                filterAndPopulateDishes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

        val filtered = allDishes.filter { dish ->
            val matchesCategory = currentCategory == "All Dishes" || dish.categoryId == currentCategory
            val matchesSpecialty = !specialtyOnly || dish.isChefSpecial
            val matchesSearch = dish.nom.lowercase(Locale.getDefault())
                .contains(currentSearchQuery.lowercase(Locale.getDefault())) ||
                dish.description.lowercase(Locale.getDefault())
                    .contains(currentSearchQuery.lowercase(Locale.getDefault()))
            matchesCategory && matchesSpecialty && matchesSearch &&
                dish.disponible && dish.isValidatedByAdmin
        }

        for (dish in filtered) {
            val item = inflater.inflate(R.layout.item_menu_dish, container, false)
            item.findViewById<TextView>(R.id.tv_title).text = dish.nom
            item.findViewById<TextView>(R.id.tv_category).text = dish.categoryId?.uppercase() ?: ""
            item.findViewById<TextView>(R.id.tv_price).text = dish.prixFormat
            val imageResId = resources.getIdentifier(dish.photoUrl, "drawable", requireContext().packageName)
            val imageView = item.findViewById<ImageView>(R.id.iv_dish_image)
            if (imageResId != 0) imageView.setImageResource(imageResId)
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
            item.setOnClickListener { navigateToDetail(dish) }
            container.addView(item)
        }
    }

    private fun navigateToDetail(dish: Dish) {
        val imageResId = resources.getIdentifier(dish.photoUrl, "drawable", requireContext().packageName)
        findNavController().navigate(
            R.id.action_menu_to_detail,
            bundleOf(
                "dishId" to dish.id,
                "title" to dish.nom,
                "price" to dish.prixFormat,
                "prixRaw" to dish.prix,
                "description" to dish.description,
                "imageRes" to imageResId
            )
        )
    }
}
