package com.karaketir.coachingapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.karaketir.coachingapp.R

object NotificationChannelRegistry {

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            channel(
                NotificationType.MESSAGE,
                context.getString(R.string.notification_channel_messages),
                context.getString(R.string.notification_channel_messages_desc),
                NotificationManager.IMPORTANCE_HIGH,
            ),
            channel(
                NotificationType.DUTY,
                context.getString(R.string.notification_channel_duties),
                context.getString(R.string.notification_channel_duties_desc),
                NotificationManager.IMPORTANCE_HIGH,
            ),
            channel(
                NotificationType.STUDY_UPDATE,
                context.getString(R.string.notification_channel_study),
                context.getString(R.string.notification_channel_study_desc),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
            channel(
                NotificationType.GENERAL,
                context.getString(R.string.notification_channel_general),
                context.getString(R.string.notification_channel_general_desc),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannels(channels)
    }

    private fun channel(
        type: NotificationType,
        name: String,
        description: String,
        importance: Int,
    ): NotificationChannel {
        return NotificationChannel(type.channelId, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
    }
}
