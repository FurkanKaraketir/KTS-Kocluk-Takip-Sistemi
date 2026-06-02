package com.karaketir.coachingapp.notifications

import androidx.core.app.NotificationCompat

enum class NotificationType(
    val channelId: String,
    val idBase: Int,
    val groupKey: String,
    val category: String,
) {
    MESSAGE(
        channelId = "kts_channel_messages",
        idBase = 1000,
        groupKey = "kts_group_messages",
        category = NotificationCompat.CATEGORY_MESSAGE,
    ),
    DUTY(
        channelId = "kts_channel_duties",
        idBase = 2000,
        groupKey = "kts_group_duties",
        category = NotificationCompat.CATEGORY_REMINDER,
    ),
    STUDY_UPDATE(
        channelId = "kts_channel_study",
        idBase = 3000,
        groupKey = "kts_group_study",
        category = NotificationCompat.CATEGORY_STATUS,
    ),
    GENERAL(
        channelId = "kts_channel_general",
        idBase = 4000,
        groupKey = "kts_group_general",
        category = NotificationCompat.CATEGORY_EVENT,
    );

    companion object {
        fun fromDataValue(value: String?): NotificationType {
            if (value.isNullOrBlank()) return GENERAL
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GENERAL
        }
    }
}
