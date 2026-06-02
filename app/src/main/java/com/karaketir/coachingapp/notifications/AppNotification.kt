package com.karaketir.coachingapp.notifications

data class AppNotification(
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
) {
    val notificationId: Int
        get() = type.idBase + (title + body).hashCode().and(0xFFFF)
}
