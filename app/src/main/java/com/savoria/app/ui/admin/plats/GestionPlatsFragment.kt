package com.savoria.app.ui.admin.plats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.ui.admin.AdminActivity
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.AdminViewModelFactory
import kotlinx.coroutines.launch

class GestionPlatsFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    private lateinit var adapter: DishAdminAdapter
    private var showPendingOnly = false
    private val isAdminHost: Boolean
        get() = activity is AdminActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_plats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DishAdminAdapter(
            onEdit = { dish ->
                if (isAdminHost) {
                    findNavController().navigate(
                        R.id.navigation_add_dish,
                        bundleOf("dishId" to dish.id)
                    )
                } else {
                    DishDialogFragment.newInstance(dish, isChefMode = true)
                        .show(childFragmentManager, DishDialogFragment.TAG)
                }
            },
            onDelete = { dish -> viewModel.deleteDish(dish) },
            onAvailabilityChanged = { dish, available ->
                viewModel.setAvailability(dish, available)
            },
            onValidate = { dish -> viewModel.validateDish(dish) },
            showValidateButton = isAdminHost
        )

        val progress = view.findViewById<CircularProgressIndicator>(R.id.progress_plats)
        view.findViewById<RecyclerView>(R.id.recyclerViewPlats).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GestionPlatsFragment.adapter
        }

        val tabs = view.findViewById<TabLayout>(R.id.tab_dish_filter)
        if (isAdminHost && tabs != null) {
            tabs.addTab(tabs.newTab().setText("Tous"))
            tabs.addTab(tabs.newTab().setText("À valider"))
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    showPendingOnly = tab?.position == 1
                    refreshList()
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            })
        } else {
            tabs?.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishesState.collect { state ->
                when (state) {
                    UiState.Loading -> progress.bindListLoading(true)
                    else -> {
                        progress.bindListLoading(false)
                        refreshList()
                    }
                }
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddDish)?.setOnClickListener {
            if (isAdminHost) {
                findNavController().navigate(R.id.navigation_add_dish)
            } else {
                DishDialogFragment.newInstance(null, isChefMode = true)
                    .show(childFragmentManager, DishDialogFragment.TAG)
            }
        }
    }

    private fun refreshList() {
        val dishes = viewModel.allDishes.value
        val filtered = if (showPendingOnly) {
            dishes.filter { !it.isValidatedByAdmin }
        } else {
            dishes
        }
        adapter.submitList(filtered)
    }
}
