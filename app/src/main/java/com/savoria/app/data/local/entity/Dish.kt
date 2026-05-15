package com.savoria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "dishes",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class Dish(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String?,
    val nom: String,
    val description: String = "",
    val prix: Double,
    val prixFormat: String = "",
    val photoUrl: String,
    val disponible: Boolean = true,
    val badgeText: String? = null,
    val badgeType: String? = null,
    val isFavorite: Boolean = false,
    val isChefSpecialty: Boolean = false,
    val prixPromo: Double? = null
) {
    /** Alias demandé par le métier : spécialité du chef. */
    val isChefSpecial: Boolean get() = isChefSpecialty
}