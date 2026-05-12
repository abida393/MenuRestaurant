package com.savoria.app.ui.util

import com.savoria.app.R

object BadgeUtils {
    fun resForType(type: String?): Int = when (type) {
        "red_small"  -> R.drawable.bg_badge_red_small
        "blue_small" -> R.drawable.bg_badge_blue_small
        "green"      -> R.drawable.bg_badge_green
        "red"        -> R.drawable.bg_badge_red
        else         -> 0
    }
}
