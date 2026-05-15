package com.savoria.app.ui.admin.plats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class GestionPlatsFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as SavoriaApplication).dishRepository)
    }

    private lateinit var adapter: DishAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_plats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DishAdminAdapter(
            onEdit = { dish ->
                DishDialogFragment.newInstance(dish)
                    .show(childFragmentManager, DishDialogFragment.TAG)
            },
            onDelete = { dish -> viewModel.deleteDish(dish) },
            onAvailabilityChanged = { dish, available ->
                viewModel.setAvailability(dish, available)
            }
        )

        view.findViewById<RecyclerView>(R.id.recyclerViewPlats).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GestionPlatsFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                adapter.submitList(dishes)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddDish).setOnClickListener {
            DishDialogFragment.newInstance(null)
                .show(childFragmentManager, DishDialogFragment.TAG)
        }
    }
}
