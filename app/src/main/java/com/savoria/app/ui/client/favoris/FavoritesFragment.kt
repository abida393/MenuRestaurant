package com.savoria.app.ui.client.favoris

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
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
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import com.savoria.app.ui.util.BadgeUtils
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.util.DishImageLoader
import com.savoria.app.ui.util.DishImageLoader.toDetailArgs
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: SharedDishViewModel
    private lateinit var listContainer: LinearLayout
    private lateinit var progressFavorites: CircularProgressIndicator
    private lateinit var tvFavoritesEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listContainer = view.findViewById(R.id.ll_favorites_list)
        progressFavorites = view.findViewById(R.id.progress_favorites)
        tvFavoritesEmpty = view.findViewById(R.id.tv_favorites_empty)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        // Admin login button
        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        // Sidebar action
        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteDishesState.collect { state ->
                when (state) {
                    UiState.Loading -> {
                        progressFavorites.bindListLoading(true)
                        tvFavoritesEmpty.visibility = View.GONE
                        listContainer.removeAllViews()
                    }
                    UiState.Empty -> {
                        progressFavorites.bindListLoading(false)
                        populateFavorites(emptyList())
                    }
                    is UiState.Success -> {
                        progressFavorites.bindListLoading(false)
                        populateFavorites(state.data)
                    }
                }
            }
        }
    }

    private fun populateFavorites(dishes: List<Dish>) {
        if (!::listContainer.isInitialized) return
        listContainer.removeAllViews()
        if (dishes.isEmpty()) {
            tvFavoritesEmpty.visibility = View.VISIBLE
            return
        }
        tvFavoritesEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(context)

        for (dish in dishes) {
            val itemView = inflater.inflate(R.layout.item_favorite_dish, listContainer, false)

            val tvCategory    = itemView.findViewById<TextView>(R.id.tv_category)
            val tvTitle       = itemView.findViewById<TextView>(R.id.tv_title)
            val tvDescription = itemView.findViewById<TextView>(R.id.tv_description)
            val tvPrice       = itemView.findViewById<TextView>(R.id.tv_price)
            val tvAction      = itemView.findViewById<TextView>(R.id.tv_action_button)
            val ivDishImage   = itemView.findViewById<ImageView>(R.id.iv_dish_image)

            tvTitle.text       = dish.nom
            tvDescription.text = dish.description
            tvPrice.text       = dish.prixFormat
            
            DishImageLoader.load(ivDishImage, dish.photoUrl)

            // Category badge styling
            if (dish.badgeText != null) {
                tvCategory.visibility = View.VISIBLE
                tvCategory.text = dish.badgeText
                if (BadgeUtils.resForType(dish.badgeType) != 0) {
                    tvCategory.setBackgroundResource(BadgeUtils.resForType(dish.badgeType))
                    tvCategory.setTextColor(Color.WHITE)
                } else {
                    tvCategory.setBackgroundColor(Color.TRANSPARENT)
                    tvCategory.setTextColor(Color.parseColor("#999990"))
                    tvCategory.setPadding(0, 0, 0, 0)
                }
            } else {
                tvCategory.visibility = View.GONE
            }

            // Standard Action Button for Favorites
            tvAction.text = "DÉTAILS"
            tvAction.setBackgroundColor(Color.TRANSPARENT)
            tvAction.setTextColor(Color.parseColor("#A02020"))
            tvAction.setPadding(0, dpToPx(6), 0, dpToPx(6))

            val navigateToDetail = {
                findNavController().navigate(R.id.action_favorites_to_detail, dish.toDetailArgs())
            }

            tvAction.setOnClickListener { navigateToDetail() }
            itemView.setOnClickListener { navigateToDetail() }

            // Image: short click for detail, long-press to remove from favorites
            val ivImage = itemView.findViewById<ImageView>(R.id.iv_dish_image)
            ivImage.setOnClickListener {
                navigateToDetail()
            }
            ivImage.setOnLongClickListener {
                viewModel.toggleFavorite(dish)
                Toast.makeText(context, "${dish.nom} retiré des favoris", Toast.LENGTH_SHORT).show()
                true
            }

            listContainer.addView(itemView)
        }
    }

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
    ).toInt()
}
