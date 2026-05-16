package com.savoria.app.ui.client.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.savoria.app.ui.SharedDishViewModel
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.bindListLoading
import com.savoria.app.ui.util.BadgeUtils
import com.savoria.app.ui.util.DishImageLoader
import com.savoria.app.ui.util.DishImageLoader.toDetailArgs
import kotlinx.coroutines.launch
import java.util.Locale

class SearchFragment : Fragment() {

    private val viewModel: SharedDishViewModel by activityViewModels()

    private lateinit var etQuery: EditText
    private lateinit var recentSection: View
    private lateinit var recentContainer: LinearLayout
    private lateinit var resultsContainer: LinearLayout

    private var clientDishes: List<Dish> = emptyList()
    private var currentQuery: String = ""
    private lateinit var progressSearch: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etQuery = view.findViewById(R.id.et_search_query)
        recentSection = view.findViewById(R.id.recent_section)
        recentContainer = view.findViewById(R.id.ll_recent_searches)
        resultsContainer = view.findViewById(R.id.ll_search_results)
        progressSearch = view.findViewById(R.id.progress_search)

        arguments?.getString(ARG_INITIAL_QUERY)?.orEmpty()?.let { initial ->
            if (initial.isNotBlank()) {
                etQuery.setText(initial)
                currentQuery = initial.trim()
            }
        }

        view.findViewById<View>(R.id.btn_search_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.btn_clear_history).setOnClickListener {
            SearchRecentStore.clear(requireContext())
            populateRecentSearches()
        }

        setupQueryInput()
        populateRecentSearches()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allDishesState.collect { state ->
                    when (state) {
                        UiState.Loading -> {
                            progressSearch.bindListLoading(true)
                            clientDishes = emptyList()
                            resultsContainer.removeAllViews()
                        }
                        UiState.Empty -> {
                            progressSearch.bindListLoading(false)
                            clientDishes = emptyList()
                            updateSuggestions()
                        }
                        is UiState.Success -> {
                            progressSearch.bindListLoading(false)
                            clientDishes = state.data.filter { it.disponible && it.isValidatedByAdmin }
                            updateSuggestions()
                        }
                    }
                }
            }
        }

        etQuery.requestFocus()
    }

    private fun setupQueryInput() {
        etQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim().orEmpty()
                updateSuggestions()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                commitSearch(currentQuery)
                true
            } else {
                false
            }
        }
    }

    private fun populateRecentSearches() {
        recentContainer.removeAllViews()
        val recent = SearchRecentStore.getRecent(requireContext())
        recentSection.isVisible = recent.isNotEmpty()
        if (recent.isEmpty()) return

        val textColors = ContextCompat.getColorStateList(requireContext(), R.color.chip_filter_text)
        val defaultState = intArrayOf()

        for (term in recent) {
            val chip = TextView(requireContext()).apply {
                text = term
                textSize = 12f
                setPadding(40, 20, 40, 20)
                setBackgroundResource(R.drawable.bg_btn_grey_pill)
                textColors?.let {
                    setTextColor(it.getColorForState(defaultState, it.defaultColor))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 16 }
                setOnClickListener {
                    etQuery.setText(term)
                    etQuery.setSelection(term.length)
                    currentQuery = term
                    updateSuggestions()
                }
            }
            recentContainer.addView(chip)
        }
    }

    private fun updateSuggestions() {
        resultsContainer.removeAllViews()
        val query = currentQuery.lowercase(Locale.getDefault())

        val matches = if (query.isEmpty()) {
            clientDishes.take(6)
        } else {
            clientDishes.filter { dish ->
                dish.nom.lowercase(Locale.getDefault()).contains(query) ||
                    dish.description.lowercase(Locale.getDefault()).contains(query) ||
                    (dish.categoryId?.lowercase(Locale.getDefault())?.contains(query) == true)
            }
        }

        if (matches.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = getString(R.string.search_no_results)
                setTextColor(0xFF777770.toInt())
                setPadding(0, 24, 0, 0)
            }
            resultsContainer.addView(empty)
            return
        }

        val inflater = LayoutInflater.from(requireContext())
        for (dish in matches) {
            val item = inflater.inflate(R.layout.item_menu_dish, resultsContainer, false)
            item.findViewById<TextView>(R.id.tv_title).text = dish.nom
            item.findViewById<TextView>(R.id.tv_category).text = dish.categoryId?.uppercase() ?: ""
            item.findViewById<TextView>(R.id.tv_price).text = dish.prixFormat
            DishImageLoader.load(item.findViewById(R.id.iv_dish_image), dish.photoUrl)
            val badge = item.findViewById<TextView>(R.id.tv_badge)
            if (dish.badgeText != null) {
                badge.text = dish.badgeText
                val badgeRes = BadgeUtils.resForType(dish.badgeType)
                if (badgeRes != 0) badge.setBackgroundResource(badgeRes)
                badge.visibility = View.VISIBLE
            } else {
                badge.visibility = View.GONE
            }
            item.setOnClickListener {
                commitSearch(currentQuery.ifBlank { dish.nom })
                navigateToDetail(dish)
            }
            resultsContainer.addView(item)
        }
    }

    private fun commitSearch(query: String) {
        if (query.isNotBlank()) {
            SearchRecentStore.addQuery(requireContext(), query)
            populateRecentSearches()
        }
    }

    private fun navigateToDetail(dish: Dish) {
        findNavController().navigate(R.id.action_search_to_detail, dish.toDetailArgs())
    }

    companion object {
        const val ARG_INITIAL_QUERY = "initialQuery"
    }
}
