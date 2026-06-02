package com.karaketir.coachingapp.notifications

import android.content.Intent
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.MessageActivity
import com.karaketir.coachingapp.R

/**
 * Parses notification tap intents (from [AppNotificationManager] or FCM `data` extras on system tray taps)
 * and applies navigation once [MainActivity] has loaded the signed-in user profile.
 */
object NotificationDeepLink {

    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    const val FCM_DATA_TYPE = "type"

    fun parseType(intent: Intent?): NotificationType? {
        if (intent == null) return null
        val raw = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE)
            ?: intent.getStringExtra(FCM_DATA_TYPE)
        if (raw.isNullOrBlank()) return null
        return NotificationType.fromDataValue(raw)
    }

    fun apply(mainActivity: MainActivity, type: NotificationType, kurumKodu: Int, isTeacher: Boolean) {
        when (type) {
            NotificationType.MESSAGE -> {
                if (isTeacher) {
                    mainActivity.startActivity(
                        Intent(mainActivity, MessageActivity::class.java).apply {
                            putExtra("kurumKodu", kurumKodu.toString())
                        },
                    )
                } else {
                    mainActivity.selectStudentBottomNav(R.id.navigation_home)
                }
            }

            NotificationType.DUTY -> {
                if (isTeacher) {
                    mainActivity.selectTeacherBottomNav(R.id.navigation_home)
                } else {
                    mainActivity.selectStudentBottomNav(R.id.navigation_duties)
                }
            }

            NotificationType.STUDY_UPDATE -> {
                mainActivity.selectStudentBottomNav(R.id.navigation_home)
            }

            NotificationType.GENERAL -> {
                if (isTeacher) {
                    mainActivity.selectTeacherBottomNav(R.id.navigation_home)
                } else {
                    mainActivity.selectStudentBottomNav(R.id.navigation_home)
                }
            }
        }
    }

    fun buildMainActivityIntent(
        context: android.content.Context,
        notification: AppNotification,
    ): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, notification.type.name)
            putExtra(FCM_DATA_TYPE, notification.type.name)
            for ((key, value) in notification.data) {
                if (key != FCM_DATA_TYPE && key != "title" && key != "body") {
                    putExtra(key, value)
                }
            }
        }
    }
}
