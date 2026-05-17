package com.savoria.app.ui.chef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import kotlinx.coroutines.launch

class ChefOrdersFragment : Fragment() {

    private val chefViewModel: ChefViewModel by activityViewModels()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_commandes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerOrders)
        val emptyView = view.findViewById<TextView>(R.id.tvEmptyOrders)

        adapter = OrderAdapter(
            onStartPreparation = { orderId -> chefViewModel.startPreparation(orderId) },
            onMarkReady = { orderId -> chefViewModel.markReady(orderId) },
            onSendExcuse = { orderId ->
                ExcuseBottomSheet.newInstance { excuse ->
                    chefViewModel.sendExcuse(orderId, excuse)
                    Toast.makeText(requireContext(), R.string.excuse_sent, Toast.LENGTH_SHORT).show()
                }.show(childFragmentManager, ExcuseBottomSheet.TAG)
            }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            chefViewModel.orderListItems.collect { items ->
                adapter.submitList(items)
                val isEmpty = items.isEmpty()
                emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        // Show navigation bar when the screen is clicked/touched anywhere
        val showNavListener = View.OnTouchListener { _, _ ->
            setSystemNavigationBarVisible(true)
            false
        }
        view.setOnTouchListener(showNavListener)
        recycler.setOnTouchListener(showNavListener)
        emptyView.setOnTouchListener(showNavListener)
    }

    override fun onResume() {
        super.onResume()
        setSystemNavigationBarVisible(false)
    }

    override fun onPause() {
        super.onPause()
        setSystemNavigationBarVisible(true)
    }

    private fun setSystemNavigationBarVisible(visible: Boolean) {
        val activity = activity ?: return
        val window = activity.window
        val decorView = window.decorView
        val controller = WindowInsetsControllerCompat(window, decorView)
        if (visible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
