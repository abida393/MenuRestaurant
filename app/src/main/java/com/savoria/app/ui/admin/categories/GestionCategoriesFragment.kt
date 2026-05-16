package com.savoria.app.ui.admin.categories

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.Category
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.AdminViewModelFactory
import kotlinx.coroutines.launch

class GestionCategoriesFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    private lateinit var categoryAdapter: CategoryAdminAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var progressCategories: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEmpty = view.findViewById(R.id.tv_categories_empty)
        progressCategories = view.findViewById(R.id.progress_categories)
        categoryAdapter = CategoryAdminAdapter(
            onCategoryClick = { showCategoryOptionsDialog(it) },
            onCategoryLongClick = { confirmDelete(it) }
        )

        view.findViewById<RecyclerView>(R.id.recycler_categories).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_category)?.setOnClickListener {
            showAddCategoryDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allCategoriesState.collect { state ->
                    when (state) {
                        UiState.Loading -> {
                            progressCategories.bindListLoading(true)
                            tvEmpty.visibility = View.GONE
                        }
                        UiState.Empty -> {
                            progressCategories.bindListLoading(false)
                            categoryAdapter.submitList(emptyList())
                            tvEmpty.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            progressCategories.bindListLoading(false)
                            categoryAdapter.submitList(state.data)
                            tvEmpty.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.category_name_hint)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.category_add_title)
            .setView(editText)
            .setPositiveButton(R.string.category_add_confirm) { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    viewModel.addCategory(name)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showCategoryOptionsDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle(category.nom)
            .setItems(
                arrayOf(
                    getString(R.string.category_rename),
                    getString(R.string.category_change_order)
                )
            ) { _, which ->
                when (which) {
                    0 -> showRenameDialog(category)
                    1 -> showChangeOrderDialog(category)
                }
            }
            .show()
    }

    private fun showRenameDialog(category: Category) {
        val editText = EditText(requireContext()).apply {
            setText(category.nom)
            hint = getString(R.string.category_name_hint)
            setSelection(category.nom.length)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.category_rename_title)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isBlank()) return@setPositiveButton
                viewModel.updateCategory(category.copy(nom = newName))
                Toast.makeText(requireContext(), R.string.category_updated, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showChangeOrderDialog(category: Category) {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(category.ordreAffichage.toString())
            hint = getString(R.string.category_order_hint)
            setSelection(text.length)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.category_order_title)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val order = editText.text.toString().trim().toIntOrNull()
                if (order == null || order < 1) {
                    Toast.makeText(
                        requireContext(),
                        R.string.category_order_invalid,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                viewModel.updateCategory(category.copy(ordreAffichage = order))
                Toast.makeText(requireContext(), R.string.category_updated, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.category_delete_title)
            .setMessage(getString(R.string.category_delete_message, category.nom))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteCategory(category)
                Toast.makeText(requireContext(), R.string.category_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
