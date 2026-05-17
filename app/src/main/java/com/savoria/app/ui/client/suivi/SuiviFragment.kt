package com.savoria.app.ui.client.suivi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.savoria.app.R
import com.savoria.app.data.local.entity.OrderStatus
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
                    is UiState.Loading -> {
                        progress.bindListLoading(true)
                        empty.visibility = View.GONE
                        recycler.visibility = View.GONE
                    }
                    is UiState.Empty -> {
                        progress.bindListLoading(false)
                        empty.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        adapter.submitList(emptyList())
                    }
                    is UiState.Success -> {
                        progress.bindListLoading(false)
                        empty.visibility = View.GONE
                        recycler.visibility = View.VISIBLE
                        adapter.submitList(state.data)
                        
                        // Update stepper based on the most recent active order
                        state.data.firstOrNull()?.order?.statut?.let { s ->
                            updateStepper(s)
                        }
                    }
                }

            }
        }
    }
 
    private fun updateStepper(status: OrderStatus) {
        val root = view ?: return
        val steps = listOf(
            root.findViewById<View>(R.id.step_1_circle) to root.findViewById<TextView>(R.id.tv_step_1),
            root.findViewById<View>(R.id.step_2_circle) to root.findViewById<TextView>(R.id.tv_step_2),
            root.findViewById<View>(R.id.step_3_circle) to root.findViewById<TextView>(R.id.tv_step_3),
            root.findViewById<View>(R.id.step_4_circle) to root.findViewById<TextView>(R.id.tv_step_4)
        )
        val lines = listOf(
            root.findViewById<View>(R.id.line_1),
            root.findViewById<View>(R.id.line_2),
            root.findViewById<View>(R.id.line_3)
        )
 
        val activeIndex = when (status) {
            OrderStatus.EN_ATTENTE -> 0
            OrderStatus.EN_PREPARATION -> 1
            OrderStatus.PRET -> 2
            OrderStatus.SERVI -> 3
        }
 
        val activeColor = 0xFFA02020.toInt()
        val inactiveColor = 0xFFE8E3DC.toInt()
        val textActiveColor = 0xFF1C1C1A.toInt()
        val textInactiveColor = 0xFF999990.toInt()
 
        steps.forEachIndexed { index, (circle, text) ->
            circle?.clearAnimation()
            if (index <= activeIndex) {
                circle?.setBackgroundResource(R.drawable.bg_step_circle_active)
                text?.setTextColor(textActiveColor)
                if (index == activeIndex && status != OrderStatus.SERVI) {
                    circle?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.pulse))
                }
            } else {
                circle?.setBackgroundResource(R.drawable.bg_step_circle_inactive)
                text?.setTextColor(textInactiveColor)
            }
        }
 
        lines.forEachIndexed { index, line ->
            if (index < activeIndex) {
                line?.setBackgroundColor(activeColor)
            } else {
                line?.setBackgroundColor(inactiveColor)
            }
        }
    }
}

