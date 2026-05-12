package com.savoria.app.ui.admin.plats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel
import kotlinx.coroutines.launch

class GestionPlatsFragment : Fragment() {

    private lateinit var viewModel: SharedDishViewModel
    private lateinit var dishContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestion_plats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dishContainer = view.findViewById(R.id.ll_dish_list)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { dishes ->
                populateDishes(dishes)
            }
        }
    }

    private fun populateDishes(dishes: List<Dish>) {
        if (!::dishContainer.isInitialized) return
        dishContainer.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for (dish in dishes) {
            val item = inflater.inflate(R.layout.item_dish_management, dishContainer, false)

            item.findViewById<TextView>(R.id.tv_dish_name).text = dish.nom
            item.findViewById<TextView>(R.id.tv_dish_description).text = dish.description
            item.findViewById<TextView>(R.id.tv_category_badge).text = dish.categoryId?.uppercase() ?: "PLAT"
            item.findViewById<TextView>(R.id.tv_dish_price).text = dish.prixFormat

            item.findViewById<ImageView>(R.id.btn_edit_dish).setOnClickListener {
                val bundle = Bundle().apply {
                    putString("dishId", dish.id)
                }
                findNavController().navigate(R.id.navigation_add_dish, bundle)
            }
            item.findViewById<ImageView>(R.id.btn_delete_dish).setOnClickListener {
                viewModel.deleteDish(dish)
                Toast.makeText(context, "${dish.nom} supprimé", Toast.LENGTH_SHORT).show()
            }

            dishContainer.addView(item)
        }
    }
}

