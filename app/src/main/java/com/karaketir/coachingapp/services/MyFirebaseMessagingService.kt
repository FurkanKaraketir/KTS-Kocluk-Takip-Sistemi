package com.karaketir.coachingapp.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karaketir.coachingapp.notifications.AppNotificationManager
import com.karaketir.coachingapp.notifications.FcmRemoteMessageMapper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Legacy payloads may still include a notification block; skip if the system already showed it.
        if (remoteMessage.notification != null && remoteMessage.data.isEmpty()) {
            return
        }
        val notification = FcmRemoteMessageMapper.toAppNotification(remoteMessage) ?: return
        AppNotificationManager.show(applicationContext, notification)
    }
}
