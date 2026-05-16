package com.savoria.app.ui.client.suivi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.savoria.app.R
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import kotlinx.coroutines.launch

class SuiviFragment : Fragment() {

    private val viewModel: SuiviViewModel by viewModels()
    private lateinit var adapter: SuiviOrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_suivi, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_suivi)
        val empty = view.findViewById<TextView>(R.id.tv_suivi_empty)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progress_suivi)

        adapter = SuiviOrderAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<View>(R.id.btn_order_more).setOnClickListener {
            findNavController().navigate(R.id.navigation_menu_client)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeOrdersState.collect { state ->
                when (state) {
                    UiState.Loading -> {
                        progress.bindListLoading(true)
                        empty.visibility = View.GONE
                        recycler.visibility = View.GONE
                        adapter.submitList(emptyList())
                    }
                    UiState.Empty -> {
                        progress.bindListLoading(false)
                        adapter.submitList(emptyList())
                        empty.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                    }
                    is UiState.Success -> {
                        progress.bindListLoading(false)
                        adapter.submitList(state.data)
                        empty.visibility = View.GONE
                        recycler.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
