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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.ui.SharedDishViewModel

class AddEditDishFragment : Fragment() {

    private lateinit var ivDishPhoto: ImageView
    private lateinit var etDishName: EditText
    private lateinit var etRegularPrice: EditText
    private lateinit var etPromoPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchAvailable: SwitchMaterial
    private lateinit var switchDishOfDay: SwitchMaterial
    private lateinit var switchFeatured: SwitchMaterial
    private lateinit var chipGroupAllergens: ChipGroup

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                ivDishPhoto.visibility = View.VISIBLE
                Glide.with(this).load(selectedImageUri).centerCrop().into(ivDishPhoto)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_dish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]

        // Bind views
        ivDishPhoto = view.findViewById(R.id.iv_dish_photo)
        etDishName = view.findViewById(R.id.et_dish_name)
        etRegularPrice = view.findViewById(R.id.et_regular_price)
        etPromoPrice = view.findViewById(R.id.et_promo_price)
        etDescription = view.findViewById(R.id.et_description)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        switchAvailable = view.findViewById(R.id.switch_available)
        switchDishOfDay = view.findViewById(R.id.switch_dish_of_day)
        switchFeatured = view.findViewById(R.id.switch_featured)
        chipGroupAllergens = view.findViewById(R.id.chip_group_allergens)

        setupCategorySpinner()
        setupAllergenChips(chipGroupAllergens)

        // Check if editing
        val dishId = arguments?.getString("dishId")
        if (dishId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.allDishes.collect { dishes ->
                    if (dishes.isNotEmpty()) {
                        val dish = dishes.find { it.id == dishId }
                        if (dish != null) {
                            etDishName.setText(dish.nom)
                            etRegularPrice.setText(dish.prix.toString())
                            etDescription.setText(dish.description)
                            switchAvailable.isChecked = dish.disponible
                            switchFeatured.isChecked = dish.isFavorite

                            val categoryIndex = when (dish.categoryId) {
                                "Mains"    -> 0
                                "Seafood"  -> 1
                                "Starters" -> 2
                                "Desserts" -> 3
                                else       -> 0
                            }
                            spinnerCategory.setSelection(categoryIndex)
                        }
                        this.cancel()
                    }
                }
            }
        }

        // Photo picker
        view.findViewById<View>(R.id.fl_photo_upload).setOnClickListener { openImagePicker() }

        // Pre-select Seafood + Dairy (matching reference)
        val seafood = view.findViewById<Chip>(R.id.chip_seafood)
        val dairy = view.findViewById<Chip>(R.id.chip_dairy)
        seafood?.isChecked = true
        dairy?.isChecked = true

        // Save
        view.findViewById<View>(R.id.btn_save_dish).setOnClickListener { saveDish() }

        // Discard
        view.findViewById<View>(R.id.btn_discard).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategorySpinner() {
        val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategories.collect { categories ->
                val names = categories.map { it.nom }
                val ids   = categories.map { it.id }
                val adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_item, names
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter

                val currentDishId = arguments?.getString("dishId")
                if (currentDishId != null) {
                    val dish = viewModel.allDishes.value.find { it.id == currentDishId }
                    val idx = ids.indexOf(dish?.categoryId)
                    if (idx >= 0) spinnerCategory.setSelection(idx)
                }
            }
        }
    }

    private fun setupAllergenChips(group: ChipGroup) {
        // "+" chip — add custom allergen
        val addChip = group.findViewById<Chip>(R.id.chip_add_allergen)
        addChip?.setOnClickListener {
            Toast.makeText(context, "Custom allergen — coming soon", Toast.LENGTH_SHORT).show()
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
            etDishName.error = "Le nom est requis"
            etDishName.requestFocus()
            return
        }
        if (TextUtils.isEmpty(priceStr)) {
            etRegularPrice.error = "Le prix est requis"
            etRegularPrice.requestFocus()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val promoPrice = etPromoPrice.text.toString().trim().toDoubleOrNull()
        val categoryId = run {
            val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
            val selectedName = spinnerCategory.selectedItem?.toString() ?: ""
            viewModel.allCategories.value.find { it.nom == selectedName }?.id ?: "Mains"
        }

        val dishId = arguments?.getString("dishId") ?: java.util.UUID.randomUUID().toString()
        
        val dish = Dish(
            id = dishId,
            nom = name,
            categoryId = categoryId,
            prix = price,
            prixPromo = promoPrice,
            prixFormat = String.format("%.2f €", price),
            description = etDescription.text.toString().trim(),
            photoUrl = selectedImageUri?.toString() ?: "dish_placeholder",
            disponible = switchAvailable.isChecked,
            isFavorite = switchFeatured.isChecked
        )

        val viewModel = ViewModelProvider(requireActivity())[SharedDishViewModel::class.java]
        viewModel.insertDish(dish)

        Toast.makeText(context, "Plat enregistré avec succès", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
