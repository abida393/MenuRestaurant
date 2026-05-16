package com.savoria.app.ui.common

import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator

fun CircularProgressIndicator.bindListLoading(isLoading: Boolean) {
    visibility = if (isLoading) View.VISIBLE else View.GONE
}
