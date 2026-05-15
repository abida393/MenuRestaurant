package com.savoria.app.ui.client.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.admin.login.LoginActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }

        view.findViewById<View>(R.id.card_mains).setOnClickListener {
            openMenuFilter(category = "Mains")
        }
        view.findViewById<View>(R.id.card_specialties).setOnClickListener {
            openMenuFilter(specialtyOnly = true)
        }
        view.findViewById<View>(R.id.card_seafood).setOnClickListener {
            openMenuFilter(category = "Seafood")
        }
        view.findViewById<View>(R.id.card_desserts).setOnClickListener {
            openMenuFilter(category = "Desserts")
        }

        view.findViewById<View>(R.id.btn_view_special).setOnClickListener {
            val dishes = viewModel.allDishes.value
                .filter { it.disponible && it.isValidatedByAdmin }
            val promo = dishes.firstOrNull { it.isChefSpecial } ?: dishes.firstOrNull()
            promo?.let { dish ->
                val imageResId = resources.getIdentifier(
                    dish.photoUrl, "drawable", requireContext().packageName
                )
                findNavController().navigate(
                    R.id.action_home_to_detail,
                    bundleOf(
                        "dishId" to dish.id,
                        "title" to dish.nom,
                        "price" to dish.prixFormat,
                        "prixRaw" to dish.prix,
                        "description" to dish.description,
                        "imageRes" to imageResId
                    )
                )
            }
        }
    }

    private fun openMenuFilter(category: String = "", specialtyOnly: Boolean = false) {
        findNavController().navigate(
            R.id.action_home_to_menu,
            bundleOf(
                "categoryFilter" to category,
                "specialtyOnly" to specialtyOnly
            )
        )
    }
}
