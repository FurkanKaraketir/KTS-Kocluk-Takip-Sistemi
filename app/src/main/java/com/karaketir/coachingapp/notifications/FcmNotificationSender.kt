package com.karaketir.coachingapp.notifications

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

/**
 * Sends push notifications via the [sendNotification] Cloud Function (FCM HTTP v1 / Admin SDK).
 * Data-only payloads are displayed by [com.karaketir.coachingapp.services.MyFirebaseMessagingService].
 */
object FcmNotificationSender {

    private const val CALLABLE_SEND_NOTIFICATION = "sendNotification"

    private val mainHandler = Handler(Looper.getMainLooper())

    @Suppress("UNUSED_PARAMETER")
    fun send(
        context: Context,
        target: String,
        type: NotificationType,
        title: String,
        body: String,
        onComplete: ((NotificationSendResult) -> Unit)? = null,
    ) {
        if (target.isBlank()) {
            deliver(onComplete, NotificationSendResult.Failure(NotificationSendFailureReason.FCM_REJECTED))
            return
        }

        val payload = hashMapOf(
            "target" to target,
            "type" to type.name,
            "title" to title,
            "body" to body,
        )

        FirebaseFunctions.getInstance()
            .getHttpsCallable(CALLABLE_SEND_NOTIFICATION)
            .call(payload)
            .addOnSuccessListener {
                deliver(onComplete, NotificationSendResult.Success)
            }
            .addOnFailureListener { error ->
                deliver(
                    onComplete,
                    NotificationSendResult.Failure(mapFunctionsError(error)),
                )
            }
    }

    private fun mapFunctionsError(error: Exception): NotificationSendFailureReason {
        val functionsError = error as? FirebaseFunctionsException ?: return NotificationSendFailureReason.NETWORK_ERROR
        return when (functionsError.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                NotificationSendFailureReason.NOT_AUTHENTICATED

            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                NotificationSendFailureReason.PERMISSION_DENIED

            FirebaseFunctionsException.Code.NOT_FOUND ->
                NotificationSendFailureReason.CLOUD_FUNCTION_NOT_FOUND

            FirebaseFunctionsException.Code.UNAVAILABLE,
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
            -> NotificationSendFailureReason.NETWORK_ERROR

            else -> NotificationSendFailureReason.FCM_REJECTED
        }
    }

    private fun deliver(
        onComplete: ((NotificationSendResult) -> Unit)?,
        result: NotificationSendResult,
    ) {
        if (onComplete == null) return
        mainHandler.post { onComplete(result) }
    }
}
