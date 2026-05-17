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
import com.savoria.app.R
import kotlinx.coroutines.launch

class ChefPlatsFragment : Fragment() {

    private val viewModel: ChefPlatsViewModel by viewModels()
    private lateinit var adapter: ChefDishAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chef_plats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_chef_dishes)
        val empty = view.findViewById<TextView>(R.id.tv_chef_dishes_empty)

        adapter = ChefDishAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dishes.collect { dishes ->
                adapter.submitList(dishes)
                val isEmpty = dishes.isEmpty()
                empty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }
}
