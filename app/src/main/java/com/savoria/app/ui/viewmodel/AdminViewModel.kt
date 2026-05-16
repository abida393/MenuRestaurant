package com.savoria.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savoria.app.data.local.dao.CategoryDao
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.dao.UserDao
import com.savoria.app.data.local.entity.Category
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.repository.DishRepository
import com.savoria.app.ui.common.UiState
import com.savoria.app.ui.common.stateInAsListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class DashboardStats(
    val totalDishes: Int = 0,
    val chefSpecialties: Int = 0,
    val unavailableDishes: Int = 0,
    val pendingValidation: Int = 0,
    val totalCategories: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayOrderCount: Int = 0
)

data class DishStat(
    val dishId: String,
    val nom: String,
    val categoryId: String?,
    val prixFormat: String,
    val prix: Double,
    val photoUrl: String,
    val orderCount: Int,
    val disponible: Boolean,
    val isValidatedByAdmin: Boolean,
    val isChefSpecialty: Boolean
)

class AdminViewModel(
    private val dishRepository: DishRepository,
    private val categoryDao: CategoryDao,
    private val userDao: UserDao,
    private val orderDao: OrderDao
) : ViewModel() {

    private val dayStartMillis: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val allDishes: StateFlow<List<Dish>> = dishRepository.allDishes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allDishesState: StateFlow<UiState<List<Dish>>> =
        dishRepository.allDishes.stateInAsListUiState(viewModelScope)

    val allCategoriesState: StateFlow<UiState<List<Category>>> =
        categoryDao.getAllCategories().stateInAsListUiState(viewModelScope)

    val pendingDishes: StateFlow<List<Dish>> = allDishes
        .map { dishes -> dishes.filter { !it.isValidatedByAdmin } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCategories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    val dashboardStats: StateFlow<DashboardStats> = combine(
        dishRepository.allDishes,
        dishRepository.pendingValidationCount,
        categoryDao.countCategories(),
        orderDao.getAllOrdersWithItems()
    ) { dishes, pendingCount, categoryCount, orders ->
        val start = dayStartMillis
        val todayOrders = orders.filter { it.order.creeLe >= start }
        DashboardStats(
            totalDishes = dishes.size,
            chefSpecialties = dishes.count { it.isChefSpecial },
            unavailableDishes = dishes.count { !it.disponible },
            pendingValidation = pendingCount,
            totalCategories = categoryCount,
            todayRevenue = todayOrders.sumOf { it.order.total },
            todayOrderCount = todayOrders.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    val topDishes: StateFlow<List<DishStat>> = combine(
        orderDao.observeDishSalesAggregates(),
        dishRepository.allDishes
    ) { aggregates, dishes ->
        val dishById = dishes.associateBy { it.id }
        aggregates.mapNotNull { row ->
            val dish = dishById[row.dishId] ?: return@mapNotNull null
            DishStat(
                dishId = dish.id,
                nom = dish.nom,
                categoryId = dish.categoryId,
                prixFormat = dish.prixFormat,
                prix = dish.prix,
                photoUrl = dish.photoUrl,
                orderCount = row.totalQuantity,
                disponible = dish.disponible,
                isValidatedByAdmin = dish.isValidatedByAdmin,
                isChefSpecialty = dish.isChefSpecialty
            )
        }.take(TOP_DISHES_LIMIT)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    init {
        refreshUsers()
    }

    fun refreshUsers() = viewModelScope.launch {
        _users.value = userDao.getAllUsers()
    }

    fun addDish(dish: Dish) = viewModelScope.launch {
        repositoryInsert(dish.copy(isValidatedByAdmin = true))
        _saveMessage.value = "Plat ajouté"
    }

    fun updateDish(dish: Dish) = viewModelScope.launch {
        dishRepository.update(dish)
        _saveMessage.value = "Plat mis à jour"
    }

    fun deleteDish(dish: Dish) = viewModelScope.launch {
        dishRepository.delete(dish)
    }

    fun validateDish(dish: Dish) = viewModelScope.launch {
        dishRepository.update(dish.copy(isValidatedByAdmin = true))
        _saveMessage.value = "Plat validé — visible pour les clients"
    }

    fun setAvailability(dish: Dish, available: Boolean) = viewModelScope.launch {
        if (dish.disponible != available) {
            dishRepository.update(dish.copy(disponible = available))
        }
    }

    fun addCategory(name: String) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@launch
        val nextOrder = (allCategories.value.maxOfOrNull { it.ordreAffichage } ?: 0) + 1
        val id = trimmed.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { UUID.randomUUID().toString() }
        categoryDao.insertCategory(
            Category(id = id, nom = trimmed, ordreAffichage = nextOrder)
        )
        _saveMessage.value = "Catégorie ajoutée"
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryDao.updateCategory(category)
        _saveMessage.value = "Catégorie mise à jour"
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryDao.deleteCategory(category)
        _saveMessage.value = "Catégorie supprimée"
    }

    fun addUser(user: User) = viewModelScope.launch {
        userDao.insertUser(user)
        refreshUsers()
        _saveMessage.value = "Utilisateur créé"
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        userDao.deleteUser(user)
        refreshUsers()
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    private suspend fun repositoryInsert(dish: Dish) {
        dishRepository.insert(dish)
    }

    companion object {
        private const val TOP_DISHES_LIMIT = 5
    }
}
