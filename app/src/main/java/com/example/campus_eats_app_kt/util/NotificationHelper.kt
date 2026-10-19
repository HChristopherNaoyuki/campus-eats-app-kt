package com.example.campus_eats_app_kt.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.campus_eats_app_kt.R

/**
 * NotificationHelper manages local push notifications for order status transitions
 * and system alerts, adhering to Android 8.0+ channels and Android 13+ runtime permissions.
 */
object NotificationHelper
{
    const val CHANNEL_ID = "campus_eats_orders"
    private const val CHANNEL_NAME = "Campus Eats Order Alerts"
    private const val CHANNEL_DESC = "Notifications for campus food order updates and status transitions."

    /**
     * Initializes the notification channel on Android 8.0 (API 26) and higher.
     */
    fun createNotificationChannel(context: Context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a local order notification if POST_NOTIFICATIONS permission is granted or not required.
     * Fails gracefully without crashing if permission is denied.
     */
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
    )
    {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                return
            }
        }

        try
        {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context))
            {
                notify(notificationId, builder.build())
            }
        }
        catch (_: Exception)
        {
            // Catch unexpected notification permission or system dispatch exceptions
        }
    }
}
