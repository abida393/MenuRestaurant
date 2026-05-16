package com.savoria.app.ui.util

import androidx.annotation.DrawableRes
import com.savoria.app.R

/**
 * Type-safe map from [photoUrl] keys stored in the database to bundled drawable resources.
 * Add an entry here when introducing a new local dish image asset.
 */
object DishDrawableResources {

    val byName: Map<String, Int> = mapOf(
        "dish_wellington" to R.drawable.dish_wellington,
        "dish_scallops" to R.drawable.dish_scallops,
        "dish_pappardelle" to R.drawable.dish_pappardelle,
        "dish_lava_sphere" to R.drawable.dish_lava_sphere,
        "dish_burrata" to R.drawable.dish_burrata,
        "dish_lamb" to R.drawable.dish_lamb,
        "dish_placeholder" to R.drawable.dish_placeholder,
        "dish_test" to R.drawable.dish_placeholder
    )

    @DrawableRes
    fun resIdForKey(key: String): Int = byName[key] ?: R.drawable.dish_placeholder

    fun isKnownLocalKey(key: String): Boolean = key in byName
}
