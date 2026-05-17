package com.savoria.app.ui.client.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.savoria.app.R

import com.savoria.app.data.local.entity.Category
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.client.cart.CartViewModel
import com.savoria.app.ui.common.UiState

import com.savoria.app.ui.common.bindListLoading
import com.savoria.app.ui.util.DishImageLoader.toDetailArgs
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private val viewModel: SharedDishViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()


    private var allDishes: List<Dish> = emptyList()
    private var currentCategoryId: String? = null
    private var specialtyOnly = false

    private lateinit var dishAdapter: MenuDishAdapter
    private lateinit var chipAdapter: MenuCategoryChipAdapter
    private lateinit var promoAdapter: MenuPromoAdapter
    private lateinit var tvMenuEmpty: TextView
    private lateinit var progressMenu: CircularProgressIndicator
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var recyclerChips: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("categoryFilter")?.takeIf { it.isNotBlank() }?.let {
            currentCategoryId = it
        }
        specialtyOnly = arguments?.getBoolean("specialtyOnly") == true

        dishAdapter = MenuDishAdapter(
            onAddToCart = { dish -> addDishToCart(dish) },
            onDishClick = { navigateToDetail(it) }
        )
        promoAdapter = MenuPromoAdapter { openPromoDish() }
        chipAdapter = MenuCategoryChipAdapter { categoryId ->
            currentCategoryId = categoryId
            specialtyOnly = false
            chipAdapter.selectedCategoryId = categoryId
            submitFilteredDishes()
        }
        chipAdapter.selectedCategoryId = currentCategoryId
        tvMenuEmpty = view.findViewById(R.id.tv_menu_empty)
        progressMenu = view.findViewById(R.id.progress_menu)
        recyclerMenu = view.findViewById(R.id.recycler_menu_dishes)
        recyclerChips = view.findViewById(R.id.recycler_category_chips)

        recyclerMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ConcatAdapter(promoAdapter, dishAdapter)
        }

        recyclerChips.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = chipAdapter
        }

        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }

        view.findViewById<View>(R.id.search_bar_container).setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_search)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allCategoriesState.collect { state ->
                    when (state) {
                        UiState.Loading -> recyclerChips.visibility = View.INVISIBLE
                        is UiState.Success -> {
                            recyclerChips.visibility = View.VISIBLE
                            populateCategoryChips(state.data)
                            resolveInitialFilter()
                            submitFilteredDishes()
                        }

                        UiState.Empty -> {
                            recyclerChips.visibility = View.VISIBLE
                            populateCategoryChips(emptyList())
                        }
                    }
                    updateLoadingIndicator()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allDishesState.collect { state ->
                    when (state) {
                        UiState.Loading -> {
                            allDishes = emptyList()
                            dishAdapter.submitList(emptyList())
                            tvMenuEmpty.visibility = View.GONE
                            promoAdapter.showPromo = false
                        }
                        UiState.Empty -> {
                            allDishes = emptyList()
                            dishAdapter.submitList(emptyList())
                            updateEmptyState(emptyList())
                        }
                        is UiState.Success -> {
                            allDishes = state.data
                            resolveInitialFilter()
                            submitFilteredDishes()
                        }
                    }
                    updateLoadingIndicator()
                }
            }
        }

    }

    private fun updateLoadingIndicator() {
        val loading = viewModel.allDishesState.value is UiState.Loading ||
            viewModel.allCategoriesState.value is UiState.Loading
        progressMenu.bindListLoading(loading)
        if (loading) {
            tvMenuEmpty.visibility = View.GONE
        }
    }

    private fun populateCategoryChips(categories: List<Category>) {
        if (specialtyOnly) {
            currentCategoryId = null
        }
        val chips = buildList {
            add(MenuCategoryChip(null, getString(R.string.menu_all_dishes)))
            categories.forEach { add(MenuCategoryChip(it.id, it.nom)) }
        }
        chipAdapter.selectedCategoryId = currentCategoryId
        chipAdapter.submitList(chips)
    }

    private fun resolveInitialFilter() {
        val filter = currentCategoryId ?: return
        val categories = (viewModel.allCategoriesState.value as? UiState.Success)?.data ?: return
        
        // If it's already a valid ID, do nothing
        if (categories.any { it.id == filter }) return

        // Otherwise, try to map the English tag to a real ID
        val resolvedId = when (filter.lowercase()) {
            "mains" -> categories.find { it.nom.lowercase().contains("plat") || it.nom.lowercase().contains("main") }?.id
            "seafood" -> categories.find { it.nom.lowercase().contains("mer") || it.nom.lowercase().contains("seafood") }?.id
            "desserts" -> categories.find { it.nom.lowercase().contains("dessert") }?.id
            "starters" -> categories.find { it.nom.lowercase().contains("entrée") || it.nom.lowercase().contains("starter") }?.id
            else -> categories.find { it.nom.equals(filter, ignoreCase = true) }?.id
        }
        
        resolvedId?.let {
            currentCategoryId = it
            chipAdapter.selectedCategoryId = it
            chipAdapter.notifyDataSetChanged()
        }
    }

    private fun submitFilteredDishes() {
        val filtered = allDishes.filter { dish ->
            val matchesCategory = currentCategoryId == null || dish.categoryId == currentCategoryId
            val matchesSpecialty = !specialtyOnly || dish.isChefSpecial
            matchesCategory && matchesSpecialty &&
                dish.disponible && dish.isValidatedByAdmin
        }
        dishAdapter.submitList(filtered)
        updateEmptyState(filtered)
    }


    private fun updateEmptyState(filtered: List<Dish>) {
        if (viewModel.allDishesState.value is UiState.Loading) return

        if (filtered.isNotEmpty()) {
            tvMenuEmpty.visibility = View.GONE
            promoAdapter.showPromo = true
            return
        }

        promoAdapter.showPromo = false
        tvMenuEmpty.visibility = View.VISIBLE
        tvMenuEmpty.setText(
            when (viewModel.allDishesState.value) {
                UiState.Empty -> R.string.menu_empty_unavailable
                is UiState.Success -> {
                    val clientDishes = allDishes.filter { it.disponible && it.isValidatedByAdmin }
                    if (clientDishes.isEmpty()) R.string.menu_empty_unavailable
                    else R.string.menu_empty_filtered
                }
                UiState.Loading -> R.string.menu_empty_loading
            }
        )
    }

    private fun openPromoDish() {
        val clientDishes = allDishes.filter { it.disponible && it.isValidatedByAdmin }
        val promo = clientDishes.firstOrNull { it.isChefSpecial }
            ?: clientDishes.firstOrNull { it.badgeText != null }
            ?: clientDishes.firstOrNull()
        promo?.let { navigateToDetail(it) }
    }

    private fun navigateToDetail(dish: Dish) {
        findNavController().navigate(R.id.action_menu_to_detail, dish.toDetailArgs())
    }
 
    private fun addDishToCart(dish: Dish) {
        cartViewModel.addToCart(dish, cartViewModel.consumptionMode.value)
        Snackbar.make(requireView(), getString(R.string.added_to_cart, dish.nom), Snackbar.LENGTH_SHORT)
            .setAction(R.string.view_cart) {
                findNavController().navigate(R.id.navigation_cart)
            }
            .show()
    }
}

