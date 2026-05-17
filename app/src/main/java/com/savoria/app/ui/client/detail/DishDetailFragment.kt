package com.savoria.app.ui.client.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.client.cart.CartViewModel
import com.savoria.app.ui.client.cart.ConsumptionModeBottomSheet
import com.savoria.app.ui.util.DishImageLoader
import com.savoria.app.ui.util.DishImageLoader.toDetailArgs
import kotlinx.coroutines.launch
import java.util.UUID

class DishDetailFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private val dishViewModel: SharedDishViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dish_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            findNavController().navigateUp()
        }

        val title = arguments?.getString("title") ?: "Plat"
        val price = arguments?.getString("price") ?: "0,00 €"
        val description = arguments?.getString("description").orEmpty()
        val photoUrl = arguments?.getString("photoUrl").orEmpty()

        view.findViewById<TextView>(R.id.tv_detail_title).text = title
        view.findViewById<TextView>(R.id.tv_detail_price).text = price
        view.findViewById<TextView>(R.id.tv_detail_description).text = description
        DishImageLoader.load(view.findViewById(R.id.iv_hero_image), photoUrl)

        val btnAdd = view.findViewById<TextView>(R.id.btn_add_to_selection)
        btnAdd.text = "AJOUTER À LA SÉLECTION — $price   "

        val dishId = arguments?.getString("dishId")
        val dish = dishViewModel.allDishes.value.find { it.id == dishId } ?: Dish(
            id = dishId ?: UUID.randomUUID().toString(),
            nom = title,
            categoryId = null,
            prix = arguments?.getDouble("prixRaw") ?: 0.0,
            prixFormat = price,
            description = description,
            photoUrl = photoUrl,
            disponible = true
        )


        btnAdd.setOnClickListener {
            val sheet = ConsumptionModeBottomSheet.newInstance(dish)
            sheet.onModeSelected = { mode, selected ->
                cartViewModel.addToCart(selected, mode)
                Snackbar.make(view, "✓ ${selected.nom} ajouté au panier", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(0xFFA02020.toInt())
                    .setTextColor(0xFFFFFFFF.toInt())
                    .setAction("Panier") {
                        findNavController().navigate(R.id.navigation_cart)
                    }
                    .show()
            }
            sheet.show(childFragmentManager, "consumption_mode")
        }

        loadSimilarDishes(view, title)
    }

    private fun loadSimilarDishes(view: View, currentTitle: String) {
        val container: LinearLayout = view.findViewById(R.id.ll_similar_dishes)

        val inflater = LayoutInflater.from(context)

        viewLifecycleOwner.lifecycleScope.launch {
            dishViewModel.allDishesState.collect { state ->

                container.removeAllViews()
                val allDishes = when (state) {
                    is UiState.Success -> state.data
                    else -> emptyList()
                }
                allDishes
                    .filter { it.disponible && it.isValidatedByAdmin && it.nom != currentTitle }
                    .take(3)
                    .forEach { dish ->
                        val itemView = inflater.inflate(R.layout.item_similar_dish, container, false)
                        itemView.findViewById<TextView>(R.id.tv_title).text = dish.nom
                        itemView.findViewById<TextView>(R.id.tv_price).text = dish.prixFormat
                        DishImageLoader.load(
                            itemView.findViewById(R.id.iv_dish_image),
                            dish.photoUrl
                        )
                        itemView.setOnClickListener {
                            findNavController().navigate(
                                R.id.navigation_dish_detail,
                                dish.toDetailArgs()
                            )
                        }
                        container.addView(itemView)
                    }
            }
        }
    }
}
