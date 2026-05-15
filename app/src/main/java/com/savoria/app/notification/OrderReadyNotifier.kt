package com.savoria.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.savoria.app.ClientActivity
import com.savoria.app.R
import com.savoria.app.data.local.ClientSessionManager
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.entity.OrderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OrderReadyNotifier(
    private val context: Context,
    private val orderDao: OrderDao,
    private val scope: CoroutineScope
) {
    private val notifiedOrderIds = mutableSetOf<String>()
    private val lastKnownStatus = mutableMapOf<String, OrderStatus>()

    fun start() {
        ensureChannel()
        val sessionId = ClientSessionManager.getSessionId(context)
        scope.launch {
            orderDao.getActiveOrdersForSession(sessionId)
                .map { orders -> orders.associate { it.order.id to it.order.statut } }
                .distinctUntilChanged()
                .collect { statuses ->
                    statuses.forEach { (orderId, status) ->
                        val previous = lastKnownStatus[orderId]
                        if (
                            status == OrderStatus.PRET &&
                            previous != null &&
                            previous != OrderStatus.PRET &&
                            !notifiedOrderIds.contains(orderId)
                        ) {
                            showReadyNotification(orderId)
                            notifiedOrderIds.add(orderId)
                        }
                        lastKnownStatus[orderId] = status
                    }
                    lastKnownStatus.keys.retainAll(statuses.keys)
                }
        }
    }

    private fun showReadyNotification(orderId: String) {
        if (!canPostNotifications()) return

        val shortId = orderId.take(6).uppercase()
        val intent = Intent(context, ClientActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_order_ready_title))
            .setContentText(
                context.getString(R.string.notification_order_ready_body, shortId)
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.notification_order_ready_body, shortId)
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(orderId.hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_orders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_orders_desc)
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
        const val CHANNEL_ID = "savoria_order_updates"
    }
}
