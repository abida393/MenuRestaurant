package com.savoria.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.savoria.app.R
import com.savoria.app.data.local.dao.DishDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DishValidationNotifier(
    private val context: Context,
    private val dishDao: DishDao,
    private val scope: CoroutineScope
) {
    // Map of dish ID to Pair(Name, ValidationStatus)
    private val lastKnownDishes = mutableMapOf<String, Pair<String, Boolean>>()
    private var isFirstLoad = true

    fun start() {
        ensureChannel()
        scope.launch {
            dishDao.getAllDishes()
                .distinctUntilChanged()
                .collect { dishes ->
                    val currentMap = dishes.associate { it.id to Pair(it.nom, it.isValidatedByAdmin) }

                    if (!isFirstLoad) {
                        // 1. Check for newly validated dishes
                        currentMap.forEach { (id, pair) ->
                            val (name, validated) = pair
                            val previous = lastKnownDishes[id]
                            if (previous != null) {
                                val (_, prevValidated) = previous
                                if (validated && !prevValidated) {
                                    // Accepted! Show notification
                                    showNotification(
                                        id.hashCode(),
                                        "Plat accepté !",
                                        "Le plat \"$name\" a été accepté et validé par l'administrateur."
                                    )
                                }
                            }
                        }

                        // 2. Check for deleted dishes (refused validation)
                        lastKnownDishes.forEach { (id, pair) ->
                            val (name, prevValidated) = pair
                            if (!currentMap.containsKey(id)) {
                                // If it was pending validation and is now deleted, it was refused!
                                if (!prevValidated) {
                                    showNotification(
                                        id.hashCode(),
                                        "Plat refusé",
                                        "Le plat \"$name\" a été refusé et rejeté par l'administrateur."
                                    )
                                }
                            }
                        }
                    } else {
                        isFirstLoad = false
                    }

                    // Update our cache
                    lastKnownDishes.clear()
                    lastKnownDishes.putAll(currentMap)
                }
        }
    }

    private fun showNotification(id: Int, title: String, text: String) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(id, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Validation des Plats",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertes de validation de vos plats par l'administrateur"
        }
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CHANNEL_ID = "savoria_dish_validation"
    }
}
