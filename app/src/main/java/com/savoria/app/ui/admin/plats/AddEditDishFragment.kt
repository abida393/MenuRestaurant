package com.savoria.app.ui.admin.plats

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.util.DishImageLoader
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.AdminViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class AddEditDishFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    private lateinit var ivDishPhoto: ImageView
    private lateinit var etDishName: EditText
    private lateinit var etRegularPrice: EditText
    private lateinit var etPromoPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchAvailable: SwitchMaterial
    private lateinit var switchFeatured: SwitchMaterial

    private var selectedImageUri: Uri? = null
    private var editingDishId: String? = null
    private var categoryIds: List<String> = emptyList()

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                ivDishPhoto.visibility = View.VISIBLE
                DishImageLoader.load(ivDishPhoto, selectedImageUri.toString())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_edit_dish, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editingDishId = arguments?.getString("dishId")

        ivDishPhoto = view.findViewById(R.id.iv_dish_photo)
        etDishName = view.findViewById(R.id.et_dish_name)
        etRegularPrice = view.findViewById(R.id.et_regular_price)
        etPromoPrice = view.findViewById(R.id.et_promo_price)
        etDescription = view.findViewById(R.id.et_description)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        switchAvailable = view.findViewById(R.id.switch_available)
        switchFeatured = view.findViewById(R.id.switch_featured)

        setupCategorySpinner()
        loadDishIfEditing()

        view.findViewById<View>(R.id.fl_photo_upload).setOnClickListener { openImagePicker() }
        view.findViewById<View>(R.id.btn_save_dish).setOnClickListener { saveDish() }
        view.findViewById<View>(R.id.btn_discard).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategorySpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { categories ->
                categoryIds = categories.map { it.id }
                val names = categories.map { it.nom }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter

                editingDishId?.let { id ->
                    val dish = viewModel.allDishes.value.find { it.id == id }
                    val idx = categoryIds.indexOf(dish?.categoryId)
                    if (idx >= 0) spinnerCategory.setSelection(idx)
                }
            }
        }
    }

    private fun loadDishIfEditing() {
        val dishId = editingDishId ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val dish = viewModel.allDishes.first { list ->
                list.any { it.id == dishId } || list.isNotEmpty()
            }.find { it.id == dishId } ?: return@launch

            etDishName.setText(dish.nom)
            etRegularPrice.setText(dish.prix.toString())
            etPromoPrice.setText(dish.prixPromo?.toString().orEmpty())
            etDescription.setText(dish.description)
            switchAvailable.isChecked = dish.disponible
            switchFeatured.isChecked = dish.isChefSpecialty

            if (dish.photoUrl.isNotBlank()) {
                ivDishPhoto.visibility = View.VISIBLE
                DishImageLoader.load(ivDishPhoto, dish.photoUrl)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun saveDish() {
        val name = etDishName.text.toString().trim()
        val priceStr = etRegularPrice.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            etDishName.error = getString(R.string.dish_name_required)
            etDishName.requestFocus()
            return
        }
        if (TextUtils.isEmpty(priceStr)) {
            etRegularPrice.error = getString(R.string.dish_price_required)
            etRegularPrice.requestFocus()
            return
        }
        if (categoryIds.isEmpty()) {
            Toast.makeText(requireContext(), R.string.category_none_available, Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val promoPrice = etPromoPrice.text.toString().trim().toDoubleOrNull()
        val categoryIndex = spinnerCategory.selectedItemPosition.coerceIn(categoryIds.indices)
        val categoryId = categoryIds[categoryIndex]
        val isSpecialty = switchFeatured.isChecked
        val existing = editingDishId?.let { id ->
            viewModel.allDishes.value.find { it.id == id }
        }

        val dish = Dish(
            id = editingDishId ?: UUID.randomUUID().toString(),
            nom = name,
            categoryId = categoryId,
            prix = price,
            prixPromo = promoPrice,
            prixFormat = String.format(Locale.FRANCE, "%.2f €", price),
            description = etDescription.text.toString().trim(),
            photoUrl = selectedImageUri?.toString()
                ?: existing?.photoUrl
                ?: "dish_placeholder",
            disponible = switchAvailable.isChecked,
            isFavorite = existing?.isFavorite ?: false,
            isChefSpecialty = isSpecialty,
            badgeText = if (isSpecialty) "SPÉCIAL" else null,
            badgeType = if (isSpecialty) "blue_small" else null,
            isValidatedByAdmin = true
        )

        if (editingDishId == null) {
            viewModel.addDish(dish)
        } else {
            viewModel.updateDish(dish)
        }

        findNavController().popBackStack()
    }
}

