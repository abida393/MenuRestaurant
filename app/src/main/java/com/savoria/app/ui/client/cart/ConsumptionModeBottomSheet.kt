package com.savoria.app.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.savoria.app.R
import com.savoria.app.data.local.entity.ConsumptionMode
import com.savoria.app.data.local.entity.Dish

class ConsumptionModeBottomSheet : BottomSheetDialogFragment() {

    var onModeSelected: ((ConsumptionMode, Dish) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_consumption_mode, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dish = dishFromArgs()

        view.findViewById<View>(R.id.btn_sur_place).setOnClickListener {
            onModeSelected?.invoke(ConsumptionMode.SUR_PLACE, dish)
            dismiss()
        }
        view.findViewById<View>(R.id.btn_emporter).setOnClickListener {
            onModeSelected?.invoke(ConsumptionMode.EMPORTER, dish)
            dismiss()
        }
    }

    private fun dishFromArgs(): Dish {
        val args = requireArguments()
        return Dish(
            id = args.getString(ARG_ID).orEmpty(),
            nom = args.getString(ARG_NOM).orEmpty(),
            categoryId = args.getString(ARG_CAT),
            prix = args.getDouble(ARG_PRIX),
            prixFormat = args.getString(ARG_PRIX_FORMAT).orEmpty(),
            description = args.getString(ARG_DESC).orEmpty(),
            photoUrl = args.getString(ARG_PHOTO).orEmpty(),
            disponible = true
        )
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_NOM = "nom"
        private const val ARG_CAT = "cat"
        private const val ARG_PRIX = "prix"
        private const val ARG_PRIX_FORMAT = "prixFormat"
        private const val ARG_DESC = "desc"
        private const val ARG_PHOTO = "photo"

        fun newInstance(dish: Dish): ConsumptionModeBottomSheet {
            return ConsumptionModeBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, dish.id)
                    putString(ARG_NOM, dish.nom)
                    putString(ARG_CAT, dish.categoryId)
                    putDouble(ARG_PRIX, dish.prix)
                    putString(ARG_PRIX_FORMAT, dish.prixFormat)
                    putString(ARG_DESC, dish.description)
                    putString(ARG_PHOTO, dish.photoUrl)
                }
            }
        }
    }
}
