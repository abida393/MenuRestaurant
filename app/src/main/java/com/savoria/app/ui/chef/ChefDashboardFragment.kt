package com.savoria.app.ui.chef

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.admin.plats.DishDialogFragment
import kotlinx.coroutines.launch

class ChefDashboardFragment : Fragment() {

    private val chefViewModel: ChefViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chef_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvServedOrders = view.findViewById<TextView>(R.id.tv_served_orders)
        val tvPendingDishes = view.findViewById<TextView>(R.id.tv_pending_dishes)
        val pendingBadge = view.findViewById<TextView>(R.id.tv_pending_validation_badge)

        view.findViewById<ImageView>(R.id.btn_logout).setOnClickListener { logout() }

        view.findViewById<View>(R.id.btn_chef_add_dish).setOnClickListener {
            DishDialogFragment.newInstance(dish = null, isChefMode = true)
                .show(childFragmentManager, DishDialogFragment.TAG)
        }

        view.findViewById<View>(R.id.btn_chef_my_dishes).setOnClickListener {
            findNavController().navigate(R.id.navigation_chef_plats)
        }
        view.findViewById<View>(R.id.card_chef_my_dishes).setOnClickListener {
            findNavController().navigate(R.id.navigation_chef_plats)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            chefViewModel.dashboardStats.collect { stats ->
                tvServedOrders.text = stats.servedOrdersCount.toString()
                tvPendingDishes.text = stats.pendingDishesCount.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            chefViewModel.pendingValidationCount.collect { count ->
                pendingBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
                pendingBadge.text = if (count > 9) "9+" else count.toString()
            }
        }
    }

    private fun logout() {
        StaffSessionManager.clearSession(requireContext())
        startActivity(
            Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        requireActivity().finish()
    }
}
