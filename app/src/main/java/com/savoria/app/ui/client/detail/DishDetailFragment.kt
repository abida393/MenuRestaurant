package com.savoria.app.ui.client.detail

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
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.client.cart.CartViewModel
import com.savoria.app.data.local.entity.Dish
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID

class DishDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dish_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind top bar events
        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            }
        }

        // Get arguments
        val title = arguments?.getString("title") ?: "Côte de Bœuf\nBraisée"
        val price = arguments?.getString("price") ?: "42,00 €"
        val description = arguments?.getString("description") ?: "Notre côte de bœuf Angus cuite lentement pendant 48 heures, nappée d'une réduction de Cabernet Sauvignon. Servi sur une polenta de maïs héritage veloutée, garni d'oignons grelots rôtis et de gremolata fraîche."
        val imageRes = arguments?.getInt("imageRes", 0) ?: 0

        val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
        val currentTitle = arguments?.getString("title") ?: ""

        // Populate views
        view.findViewById<TextView>(R.id.tv_detail_title).text = title
        view.findViewById<TextView>(R.id.tv_detail_price).text = price
        view.findViewById<TextView>(R.id.tv_detail_description).text = description
        if (imageRes != 0) {
            view.findViewById<ImageView>(R.id.iv_hero_image).setImageResource(imageRes)
        }

        val btnAddToSelection = view.findViewById<TextView>(R.id.btn_add_to_selection)
        btnAddToSelection.text = "AJOUTER À LA SÉLECTION — $price   "

        // Add to Selection Action — using CartViewModel
        val cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        btnAddToSelection.setOnClickListener {
            val currentDish = Dish(
                id = arguments?.getString("dishId") ?: UUID.randomUUID().toString(),
                nom = title,
                categoryId = null,
                prix = arguments?.getDouble("prixRaw") ?: 0.0,
                prixFormat = price,
                description = description,
                photoUrl = "",
                disponible = true
            )
            cartViewModel.addToCart(currentDish)
            Toast.makeText(context, "$title ajouté au panier", Toast.LENGTH_SHORT).show()
        }

        // Populate Similar Dishes from database
        val similarDishesContainer: LinearLayout = view.findViewById(R.id.ll_similar_dishes)
        val inflater = LayoutInflater.from(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allDishes.collect { allDishes ->
                similarDishesContainer.removeAllViews()
                val similar = allDishes.filter { it.nom != currentTitle }.take(3)
                for (dish in similar) {
                    val itemView = inflater.inflate(R.layout.item_similar_dish, similarDishesContainer, false)
                    itemView.findViewById<TextView>(R.id.tv_title).text = dish.nom
                    itemView.findViewById<TextView>(R.id.tv_price).text = dish.prixFormat
                    itemView.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("title", dish.nom)
                            putString("price", dish.prixFormat)
                            putString("description", dish.description)
                            putInt("imageRes", 0)
                        }
                        findNavController().navigate(R.id.action_detail_pop)
                        findNavController().navigate(R.id.navigation_dish_detail, bundle)
                    }
                    similarDishesContainer.addView(itemView)
                }
                this.cancel()
            }
        }
    }
}
