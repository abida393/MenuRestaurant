package com.savoria.app.ui.chef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.savoria.app.R
import kotlinx.coroutines.launch

class ChefOrdersFragment : Fragment() {

    private val viewModel: ChefOrdersViewModel by viewModels()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chef_orders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerOrders)
        val emptyView = view.findViewById<TextView>(R.id.tvEmptyOrders)

        adapter = OrderAdapter(
            onStartPreparation = { orderId -> viewModel.startPreparation(orderId) },
            onMarkReady = { orderId -> viewModel.markReady(orderId) },
            onSendExcuse = { orderId ->
                ExcuseBottomSheet.newInstance { excuse ->
                    viewModel.sendExcuse(orderId, excuse)
                    Toast.makeText(requireContext(), R.string.excuse_sent, Toast.LENGTH_SHORT).show()
                }.show(childFragmentManager, ExcuseBottomSheet.TAG)
            }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                adapter.submitList(orders)
                val isEmpty = orders.isEmpty()
                emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }
}
