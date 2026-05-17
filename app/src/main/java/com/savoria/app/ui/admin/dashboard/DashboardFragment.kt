package com.savoria.app.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.AdminViewModelFactory
import com.savoria.app.ui.util.DishImageLoader
import com.savoria.app.ui.viewmodel.DishStat
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal = view.findViewById<TextView>(R.id.tv_total_dishes)
        val tvSpecialties = view.findViewById<TextView>(R.id.tv_chef_specialties)
        val tvUnavailable = view.findViewById<TextView>(R.id.tv_pending_validation)

        val tvRevenue = view.findViewById<TextView>(R.id.tv_total_revenue)
        val tvOrdersCount = view.findViewById<TextView>(R.id.tv_orders_count)
        val popularContainer = view.findViewById<LinearLayout>(R.id.ll_popular_dishes)

        view.findViewById<View>(R.id.btn_add_dish).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add_dish)
        }
        view.findViewById<View>(R.id.btn_manage_categories).setOnClickListener {
            findNavController().navigate(R.id.navigation_categories)
        }
        view.findViewById<View>(R.id.tv_view_all).setOnClickListener {
            findNavController().navigate(R.id.navigation_plats)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardStats.collect { stats ->
                    tvTotal.text = stats.totalDishes.toString()
                    tvSpecialties.text = stats.chefSpecialties.toString()
                    tvUnavailable.text = stats.pendingValidation.toString()
                    tvRevenue.text = String.format(Locale.FRANCE, "%.2f €", stats.todayRevenue)
                    tvOrdersCount.text = getString(
                        R.string.dashboard_orders_today,
                        stats.todayOrderCount
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.topDishes.collect { stats ->
                    populateTopDishes(popularContainer, stats)
                }
            }
        }
    }

    private fun populateTopDishes(container: LinearLayout, stats: List<DishStat>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (stats.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = getString(R.string.dashboard_no_top_dishes)
                setTextColor(0xFF777770.toInt())
                setPadding(0, 8, 0, 8)
            }
            container.addView(empty)
            return
        }

        for (stat in stats) {
            val item = inflater.inflate(R.layout.item_popular_dish, container, false)
            item.findViewById<TextView>(R.id.tv_dish_name).text = stat.nom
            item.findViewById<TextView>(R.id.tv_dish_meta).text = buildString {
                append(stat.categoryId ?: getString(R.string.dashboard_no_category))
                append(" • ")
                append(
                    resources.getQuantityString(
                        R.plurals.dashboard_dish_orders,
                        stat.orderCount,
                        stat.orderCount
                    )
                )
            }
            item.findViewById<TextView>(R.id.tv_dish_price).text =
                stat.prixFormat.ifBlank {
                    String.format(Locale.FRANCE, "%.2f €", stat.prix)
                }

            DishImageLoader.load(item.findViewById(R.id.iv_dish), stat.photoUrl)

            val badge = item.findViewById<TextView>(R.id.tv_stock_badge)
            when {
                !stat.isValidatedByAdmin -> {
                    badge.text = getString(R.string.admin_pending_validation)
                    badge.setBackgroundResource(R.drawable.bg_badge_red)
                    badge.setTextColor(0xFFC0392B.toInt())
                }
                stat.disponible -> {
                    badge.text = getString(R.string.dashboard_in_stock)
                    badge.setBackgroundResource(R.drawable.bg_badge_green)
                    badge.setTextColor(0xFF22A060.toInt())
                }
                else -> {
                    badge.text = getString(R.string.dashboard_out_of_stock)
                    badge.setBackgroundResource(R.drawable.bg_badge_red)
                    badge.setTextColor(0xFFC0392B.toInt())
                }
            }
            container.addView(item)
        }
    }
}
