package com.karaketir.coachingapp.notifications

import com.google.firebase.messaging.RemoteMessage

object FcmRemoteMessageMapper {

    fun toAppNotification(message: RemoteMessage): AppNotification? {
        val data = message.data
        val type = NotificationType.fromDataValue(data["type"])
        val title = data["title"]?.takeIf { it.isNotBlank() } ?: message.notification?.title
        val body = data["body"]?.takeIf { it.isNotBlank() } ?: message.notification?.body
        if (title.isNullOrBlank() && body.isNullOrBlank()) return null
        return AppNotification(
            type = type,
            title = title.orEmpty(),
            body = body.orEmpty(),
            data = data,
        )
    }
}
