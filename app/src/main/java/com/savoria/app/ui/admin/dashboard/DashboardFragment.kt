package com.savoria.app.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.savoria.app.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Add Dish screen
        view.findViewById<View>(R.id.btn_add_dish).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add_dish)
        }

        view.findViewById<View>(R.id.btn_manage_categories).setOnClickListener {
            Toast.makeText(context, "Manage Categories — coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.tv_view_all).setOnClickListener {
            Toast.makeText(context, "Full Inventory — coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_view_promos).setOnClickListener {
            Toast.makeText(context, "Promotions — coming soon", Toast.LENGTH_SHORT).show()
        }

        // Populate popular dishes with mock data
        populateMockDishes(view)
    }

    private fun populateMockDishes(root: View) {
        val container: LinearLayout = root.findViewById(R.id.ll_popular_dishes)
        val inflater = LayoutInflater.from(context)

        val dishes = arrayOf(
            arrayOf("Signature Beef Tartare", "Appetizers • 42 orders today", "$24.00", "IN"),
            arrayOf("Wild Atlantic Salmon", "Main Courses • 38 orders today", "$38.00", "LOW"),
            arrayOf("Black Truffle Tagliatelle", "Main Courses • 55 orders today", "$45.00", "IN")
        )

        for (dish in dishes) {
            val item = inflater.inflate(R.layout.item_popular_dish, container, false)

            item.findViewById<TextView>(R.id.tv_dish_name).text = dish[0]
            item.findViewById<TextView>(R.id.tv_dish_meta).text = dish[1]
            item.findViewById<TextView>(R.id.tv_dish_price).text = dish[2]

            val badge = item.findViewById<TextView>(R.id.tv_stock_badge)
            if ("IN" == dish[3]) {
                badge.text = "IN STOCK"
                badge.setBackgroundResource(R.drawable.bg_badge_green)
                badge.setTextColor(0xFF22A060.toInt())
            } else {
                badge.text = "LOW STOCK"
                badge.setBackgroundResource(R.drawable.bg_badge_red)
                badge.setTextColor(0xFFC0392B.toInt())
            }

            container.addView(item)
        }
    }
}

