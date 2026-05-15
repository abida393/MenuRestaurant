package com.savoria.app.ui.chef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.savoria.app.R

class ExcuseBottomSheet : BottomSheetDialogFragment() {

    private var onExcuseSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_excuse, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val excuses = listOf(
            R.id.excuse_ingredient to getString(R.string.excuse_ingredient),
            R.id.excuse_sold_out to getString(R.string.excuse_sold_out),
            R.id.excuse_kitchen_error to getString(R.string.excuse_kitchen_error)
        )
        excuses.forEach { (id, label) ->
            view.findViewById<TextView>(id).setOnClickListener {
                onExcuseSelected?.invoke(label)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "ExcuseBottomSheet"

        fun newInstance(onSelected: (String) -> Unit): ExcuseBottomSheet {
            return ExcuseBottomSheet().apply {
                onExcuseSelected = onSelected
            }
        }
    }
}
