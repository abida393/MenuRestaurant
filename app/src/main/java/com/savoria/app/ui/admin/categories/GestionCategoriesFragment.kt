package com.savoria.app.ui.admin.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.Category
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.SavoriaApplication
import kotlinx.coroutines.launch

class GestionCategoriesFragment : Fragment() {
    private lateinit var viewModel: SharedDishViewModel
    private lateinit var container: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_gestion_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.container = view.findViewById(R.id.ll_category_list)
        viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
        
        view.findViewById<FloatingActionButton>(R.id.fab_add_category)?.setOnClickListener {
            showAddCategoryDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { cats -> populateCategories(cats) }
        }
    }

    private fun showAddCategoryDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Nom de la catégorie (ex: Pizzas)"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Nouvelle Catégorie")
            .setView(editText)
            .setPositiveButton("Ajouter") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    val newCat = Category(nom = name, ordreAffichage = 0)
                    saveCategory(newCat)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun saveCategory(category: Category) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = SavoriaDatabase.getDatabase(requireContext(), (requireActivity().application as SavoriaApplication).applicationScope)
            db.categoryDao().insertCategory(category)
            Toast.makeText(context, "Catégorie ajoutée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateCategories(categories: List<Category>) {
        container.removeAllViews()
        for (cat in categories) {
            val item = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, container, false)
            val tv = item.findViewById<TextView>(android.R.id.text1)
            tv.text = cat.nom
            container.addView(item)
        }
    }
}
