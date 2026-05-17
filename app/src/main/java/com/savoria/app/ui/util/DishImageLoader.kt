package com.savoria.app.ui.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.savoria.app.R
import com.savoria.app.data.local.entity.Dish
import java.io.File
import java.io.FileOutputStream

object DishImageLoader {

    fun saveImageLocally(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val directory = File(context.filesDir, "dish_images").apply { mkdirs() }
            val file = File(directory, "dish_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun load(imageView: ImageView, photoUrl: String?) {
        val source = photoUrl?.trim().orEmpty()
        val request = Glide.with(imageView)
            .load(resolveModel(source))
            .centerCrop()
            .placeholder(R.drawable.dish_placeholder)
            .error(R.drawable.dish_placeholder)

        request.into(imageView)
    }

    fun clear(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
        imageView.setImageResource(R.drawable.dish_placeholder)
    }

    /** Resolves [photoUrl] to a Glide model: remote URL, content URI, or @DrawableRes id. */
    fun resolveModel(photoUrl: String): Any? {
        if (photoUrl.isEmpty()) return R.drawable.dish_placeholder
        return when {
            isRemoteUrl(photoUrl) -> photoUrl
            photoUrl.startsWith("content:") -> Uri.parse(photoUrl)
            photoUrl.startsWith("file:") -> Uri.parse(photoUrl)
            else -> DishDrawableResources.resIdForKey(photoUrl)
        }
    }

    fun isRemoteUrl(photoUrl: String): Boolean =
        photoUrl.startsWith("http://", ignoreCase = true) ||
            photoUrl.startsWith("https://", ignoreCase = true)

    fun isLocalDrawableKey(photoUrl: String): Boolean =
        !isRemoteUrl(photoUrl) && !photoUrl.startsWith("content:")

    /** Navigation args for [com.savoria.app.ui.client.detail.DishDetailFragment]. */
    fun Dish.toDetailArgs() = bundleOf(
        "dishId" to id,
        "title" to nom,
        "price" to prixFormat,
        "prixRaw" to prix,
        "description" to description,
        "photoUrl" to photoUrl
    )
}
