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

    private var currentCategory = "All Dishes"
    private var currentSearchQuery = ""
    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.ll_dish_list)
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

        // Filter chips setup
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
                // Visual selection feedback
                chips.forEach { c ->
                    c.setBackgroundResource(R.drawable.bg_btn_grey_pill)
                    c.setTextColor(0xFF555555.toInt())
                }
                chip.setBackgroundResource(R.drawable.bg_stat_card_dark)
                chip.setTextColor(0xFFFFFFFF.toInt())
                
                currentCategory = categoryMap[chip] ?: "All Dishes"
                filterAndPopulateDishes()
            }
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

        // Observe ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                allDishes = dishes
                filterAndPopulateDishes()
            }
        }
    }

    private fun filterAndPopulateDishes() {
        if (!::container.isInitialized) return
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        val filteredDishes = allDishes.filter { dish ->
            val matchesCategory = currentCategory == "All Dishes" || dish.categoryId == currentCategory
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
