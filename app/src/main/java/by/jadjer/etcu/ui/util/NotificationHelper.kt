package by.jadjer.etcu.ui.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import by.jadjer.etcu.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ALERTS = "alerts"
        const val CHANNEL_CRUISE = "cruise"

        const val NOTIF_ID_ENGINE_TEMP = 1001
        const val NOTIF_ID_SERVO_TEMP = 1002
        const val NOTIF_ID_VOLTAGE = 1003
        const val NOTIF_ID_CRUISE = 1004
        const val NOTIF_ID_CRUISE_FAIL = 1005
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS,
            context.getString(R.string.notif_channel_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical vehicle and system alerts"
        }

        val cruiseChannel = NotificationChannel(
            CHANNEL_CRUISE,
            context.getString(R.string.notif_channel_cruise),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Cruise control status updates"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(alertsChannel)
        manager.createNotificationChannel(cruiseChannel)
    }

    @SuppressLint("MissingPermission")
    fun showNotification(id: Int, channelId: String, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_throttle)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (channelId == CHANNEL_ALERTS) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(id, builder.build())
        } catch (_: SecurityException) {
            // Handle missing permission for Android 13+
        }
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
}
