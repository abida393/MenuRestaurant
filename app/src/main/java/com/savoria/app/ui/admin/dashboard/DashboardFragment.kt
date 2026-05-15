package com.savoria.app.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.relation.OrderWithItems
import com.savoria.app.ui.SharedDishViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var viewModel: SharedDishViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        // Navigate to Add Dish screen
        view.findViewById<View>(R.id.btn_add_dish).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add_dish)
        }

        view.findViewById<View>(R.id.btn_manage_categories).setOnClickListener {
            findNavController().navigate(R.id.navigation_categories)
        }

        view.findViewById<View>(R.id.tv_view_all).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_plats)
        }

        view.findViewById<View>(R.id.iv_avatar).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_users)
        }

        // Observe Data for Stats
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                updateDishStats(view, dishes)
                populatePopularDishes(view, dishes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { categories ->
                view.findViewById<TextView>(R.id.tv_total_categories).text = categories.size.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allOrders.collect { orders ->
                updateOrderStats(view, orders)
            }
        }
    }

    private fun updateDishStats(root: View, dishes: List<Dish>) {
        root.findViewById<TextView>(R.id.tv_total_dishes).text = dishes.size.toString()
        
        val promoCount = dishes.count { it.prixPromo != null && it.prixPromo > 0 }
        root.findViewById<TextView>(R.id.tv_active_promos).text = String.format(Locale.getDefault(), "%02d", promoCount)
    }

    private fun updateOrderStats(root: View, orders: List<OrderWithItems>) {
        val totalRevenue = orders.sumOf { it.order.total }
        root.findViewById<TextView>(R.id.tv_total_revenue).text = String.format(Locale.getDefault(), "%.2f €", totalRevenue)
        
        val orderCount = orders.size
        root.findViewById<TextView>(R.id.tv_orders_count).text = if (orderCount <= 1) {
            "$orderCount commande aujourd'hui"
        } else {
            "$orderCount commandes aujourd'hui"
        }
    }

    private fun populatePopularDishes(root: View, dishes: List<Dish>) {
        val container: LinearLayout = root.findViewById(R.id.ll_popular_dishes)
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        // For now, let's take favorite dishes as "popular" or just the first few
        val popularOnes = dishes.filter { it.isFavorite }.take(3)

        if (popularOnes.isEmpty() && dishes.isNotEmpty()) {
            // Fallback if no favorites
            displayDishes(container, dishes.take(3), inflater)
        } else {
            displayDishes(container, popularOnes, inflater)
        }
    }

    private fun displayDishes(container: LinearLayout, list: List<Dish>, inflater: LayoutInflater) {
        for (dish in list) {
            val item = inflater.inflate(R.layout.item_popular_dish, container, false)

            item.findViewById<TextView>(R.id.tv_dish_name).text = dish.nom
            val metaText = "${dish.categoryId} • Populaire"
            item.findViewById<TextView>(R.id.tv_dish_meta).text = metaText
            item.findViewById<TextView>(R.id.tv_dish_price).text = dish.prixFormat

            val badge = item.findViewById<TextView>(R.id.tv_stock_badge)
            if (dish.disponible) {
                badge.text = "EN STOCK"
                badge.setBackgroundResource(R.drawable.bg_badge_green)
                badge.setTextColor(0xFF22A060.toInt())
            } else {
                badge.text = "ÉPUISÉ"
                badge.setBackgroundResource(R.drawable.bg_badge_red)
                badge.setTextColor(0xFFC0392B.toInt())
            }

            container.addView(item)
        }
    }
}

