package com.savoria.app.ui.serveur

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import kotlinx.coroutines.launch

class ServeurOrdersFragment : Fragment() {

    private val viewModel: ServeurViewModel by activityViewModels {
        ServeurViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    private lateinit var adapter: ServeurOrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_serveur_orders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_serveur_orders)
        val emptyView = view.findViewById<TextView>(R.id.tv_empty_serveur_orders)

        adapter = ServeurOrderAdapter { orderId ->
            viewModel.markServed(orderId)
            Toast.makeText(requireContext(), R.string.serveur_marked_served, Toast.LENGTH_SHORT).show()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.readyOrders.collect { orders ->
                    adapter.submitList(orders)
                    val isEmpty = orders.isEmpty()
                    emptyView.isVisible = isEmpty
                    recycler.isVisible = !isEmpty
                }
            }
        }
    }
}
