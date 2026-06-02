package com.karaketir.coachingapp.notifications

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.karaketir.coachingapp.R

object AppNotificationManager {

    fun show(context: Context, notification: AppNotification) {
        NotificationChannelRegistry.ensureCreated(context)

        val launchIntent = NotificationDeepLink.buildMainActivityIntent(context, notification)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val built = NotificationCompat.Builder(context, notification.type.channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(notification.type.category)
            .setGroup(notification.type.groupKey)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .build()

        NotificationManagerCompat.from(context).notify(notification.notificationId, built)
    }
}
