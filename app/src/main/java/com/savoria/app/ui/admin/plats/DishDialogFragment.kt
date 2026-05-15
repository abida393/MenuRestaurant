package com.savoria.app.ui.admin.plats

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.databinding.DialogDishBinding
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID

class DishDialogFragment : DialogFragment() {

    private var _binding: DialogDishBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by activityViewModels {
        ViewModelFactory((requireActivity().application as SavoriaApplication).dishRepository)
    }

    private var existingDishId: String? = null
    private var existingDisponible: Boolean = true
    private var existingIsFavorite: Boolean = false
    private var existingIsValidated: Boolean = false
    private var isChefMode: Boolean = false
    private var photoUrl: String = ""
    private var categoryIds: List<String> = emptyList()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = it.toString()
            binding.imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(it).centerCrop().into(binding.imgPreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoUrl = arguments?.getString(ARG_PHOTO).orEmpty()
        if (photoUrl.isNotBlank()) {
            binding.imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(photoUrl).centerCrop().into(binding.imgPreview)
        }

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val app = requireActivity().application as SavoriaApplication
        viewLifecycleOwner.lifecycleScope.launch {
            app.database.categoryDao().getAllCategories().collect { categories ->
                categoryIds = categories.map { it.id }
                val names = categories.map { it.nom }
                binding.spinnerCategory.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    names
                )
                arguments?.getString(ARG_CAT)?.let { catId ->
                    val index = categoryIds.indexOf(catId)
                    if (index >= 0) binding.spinnerCategory.setSelection(index)
                }
            }
        }

        isChefMode = arguments?.getBoolean(ARG_CHEF_MODE, false) == true

        arguments?.let { args ->
            existingDishId = args.getString(ARG_ID)
            existingDisponible = args.getBoolean(ARG_DISPONIBLE, true)
            existingIsFavorite = args.getBoolean(ARG_FAVORITE, false)
            existingIsValidated = args.getBoolean(ARG_VALIDATED, false)
            binding.textTitle.text = "Modifier le plat"
            binding.editName.setText(args.getString(ARG_NOM))
            binding.editDescription.setText(args.getString(ARG_DESC))
            binding.editPrice.setText(args.getDouble(ARG_PRIX).toString())
            binding.switchChefSpecialty.isChecked = args.getBoolean(ARG_SPECIALTY, false)
        } ?: run {
            binding.textTitle.text = "Ajouter un plat"
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun save() {
        val nom = binding.editName.text?.toString()?.trim().orEmpty()
        val prix = binding.editPrice.text?.toString()?.trim()?.toDoubleOrNull()

        if (nom.isBlank() || prix == null) {
            Toast.makeText(requireContext(), "Nom et prix obligatoires", Toast.LENGTH_SHORT).show()
            return
        }
        if (categoryIds.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune catégorie disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = binding.spinnerCategory.selectedItemPosition
        val categoryId = categoryIds.getOrElse(selectedIndex) { categoryIds.first() }
        val isSpecialty = binding.switchChefSpecialty.isChecked

        val isNew = existingDishId == null
        val validated = when {
            isChefMode && isNew -> false
            isChefMode -> existingIsValidated
            else -> true
        }

        val dish = Dish(
            id = existingDishId ?: UUID.randomUUID().toString(),
            nom = nom,
            description = binding.editDescription.text?.toString().orEmpty(),
            prix = prix,
            prixFormat = String.format("%.2f €", prix),
            categoryId = categoryId,
            photoUrl = photoUrl.ifBlank { "dish_placeholder" },
            disponible = existingDisponible,
            isFavorite = existingIsFavorite,
            isChefSpecialty = isSpecialty,
            badgeText = if (isSpecialty) "SPÉCIAL" else null,
            badgeType = if (isSpecialty) "blue_small" else null,
            isValidatedByAdmin = validated
        )

        if (isNew) viewModel.addDish(dish) else viewModel.updateDish(dish)
        if (isChefMode && isNew) {
            Toast.makeText(
                requireContext(),
                R.string.dish_pending_validation,
                Toast.LENGTH_LONG
            ).show()
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DishDialogFragment"
        private const val ARG_ID = "arg_id"
        private const val ARG_NOM = "arg_nom"
        private const val ARG_DESC = "arg_desc"
        private const val ARG_PRIX = "arg_prix"
        private const val ARG_CAT = "arg_cat"
        private const val ARG_SPECIALTY = "arg_specialty"
        private const val ARG_DISPONIBLE = "arg_disponible"
        private const val ARG_FAVORITE = "arg_favorite"
        private const val ARG_PHOTO = "arg_photo"
        private const val ARG_VALIDATED = "arg_validated"
        private const val ARG_CHEF_MODE = "arg_chef_mode"

        fun newInstance(dish: Dish?, isChefMode: Boolean = false): DishDialogFragment {
            val fragment = DishDialogFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(ARG_CHEF_MODE, isChefMode)
                dish?.let {
                    putString(ARG_ID, it.id)
                    putString(ARG_NOM, it.nom)
                    putString(ARG_DESC, it.description)
                    putDouble(ARG_PRIX, it.prix)
                    putString(ARG_CAT, it.categoryId)
                    putBoolean(ARG_SPECIALTY, it.isChefSpecialty)
                    putBoolean(ARG_DISPONIBLE, it.disponible)
                    putBoolean(ARG_FAVORITE, it.isFavorite)
                    putBoolean(ARG_VALIDATED, it.isValidatedByAdmin)
                    putString(ARG_PHOTO, it.photoUrl)
                }
            }
            return fragment
        }
    }
}
