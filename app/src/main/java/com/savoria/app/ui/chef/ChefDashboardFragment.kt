package com.savoria.app.ui.chef

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.admin.plats.DishDialogFragment
import kotlinx.coroutines.launch

class ChefDashboardFragment : Fragment() {

    private val dashboardViewModel: ChefDashboardViewModel by viewModels()
    private val ordersViewModel: ChefOrdersViewModel by viewModels()

    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chef_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_dashboard_orders)
        val emptyOrders = view.findViewById<TextView>(R.id.tv_dashboard_orders_empty)
        val tvCompleted = view.findViewById<TextView>(R.id.tv_completed_orders)
        val pendingBadge = view.findViewById<TextView>(R.id.tv_pending_validation_badge)

        orderAdapter = OrderAdapter(
            onStartPreparation = { ordersViewModel.startPreparation(it) },
            onMarkReady = { ordersViewModel.markReady(it) },
            onSendExcuse = { orderId ->
                ExcuseBottomSheet.newInstance { excuse ->
                    ordersViewModel.sendExcuse(orderId, excuse)
                    Toast.makeText(
                        requireContext(),
                        R.string.excuse_sent,
                        Toast.LENGTH_SHORT
                    ).show()
                }.show(childFragmentManager, ExcuseBottomSheet.TAG)
            }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = orderAdapter

        view.findViewById<ImageView>(R.id.btn_logout).setOnClickListener { logout() }

        view.findViewById<View>(R.id.btn_chef_add_dish).setOnClickListener {
            DishDialogFragment.newInstance(dish = null, isChefMode = true)
                .show(childFragmentManager, DishDialogFragment.TAG)
        }

        view.findViewById<ImageButton>(R.id.btn_chef_my_dishes).setOnClickListener {
            findNavController().navigate(R.id.navigation_chef_plats)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.stats.collect { stats ->
                tvCompleted.text = stats.completedOrdersCount.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.pendingValidationCount.collect { count ->
                pendingBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
                pendingBadge.text = count.coerceAtMost(9).toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            ordersViewModel.orders.collect { orders ->
                orderAdapter.submitList(orders)
                val isEmpty = orders.isEmpty()
                emptyOrders.visibility = if (isEmpty) View.VISIBLE else View.GONE
                recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
