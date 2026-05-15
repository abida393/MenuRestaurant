package com.savoria.app.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as SavoriaApplication).dishRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal = view.findViewById<TextView>(R.id.tv_total_dishes)
        val tvSpecialties = view.findViewById<TextView>(R.id.tv_total_categories)
        val tvUnavailable = view.findViewById<TextView>(R.id.tv_active_promos)
        val popularContainer = view.findViewById<LinearLayout>(R.id.ll_popular_dishes)

        view.findViewById<View>(R.id.btn_add_dish).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add_dish)
        }
        view.findViewById<View>(R.id.tv_view_all).setOnClickListener {
            findNavController().navigate(R.id.navigation_plats)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardStats.collect { stats ->
                tvTotal.text = stats.totalDishes.toString()
                tvSpecialties.text = stats.chefSpecialties.toString()
                tvUnavailable.text = stats.unavailableDishes.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                popularContainer.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())
                dishes
                    .sortedByDescending { it.isChefSpecialty }
                    .take(3)
                    .forEach { dish ->
                        val item = inflater.inflate(R.layout.item_popular_dish, popularContainer, false)
                        item.findViewById<TextView>(R.id.tv_dish_name).text = dish.nom
                        item.findViewById<TextView>(R.id.tv_dish_meta).text =
                            buildString {
                                append(dish.categoryId ?: "Sans catégorie")
                                if (dish.isChefSpecial) append(" • Spécialité du chef")
                                if (!dish.isValidatedByAdmin) append(" • En attente de validation")
                            }
                        item.findViewById<TextView>(R.id.tv_dish_price).text =
                            dish.prixFormat.ifBlank { "%.2f €".format(dish.prix) }

                        val badge = item.findViewById<TextView>(R.id.tv_stock_badge)
                        if (dish.disponible) {
                            badge.text = "DISPONIBLE"
                            badge.setBackgroundResource(R.drawable.bg_badge_green)
                            badge.setTextColor(0xFF22A060.toInt())
                        } else {
                            badge.text = "INDISPONIBLE"
                            badge.setBackgroundResource(R.drawable.bg_badge_red)
                            badge.setTextColor(0xFFC0392B.toInt())
                        }
                        popularContainer.addView(item)
                    }
            }
        }
    }
}
