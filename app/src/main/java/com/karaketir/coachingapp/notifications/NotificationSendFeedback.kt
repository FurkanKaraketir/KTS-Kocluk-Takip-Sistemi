package com.karaketir.coachingapp.notifications

import android.content.Context
import android.widget.Toast
import com.karaketir.coachingapp.R

/**
 * User-visible feedback for outbound FCM sends (Turkish strings from [R.string]).
 */
object NotificationSendFeedback {

    /**
     * @param primarySaveSucceeded When true, the main action (Firestore save) already succeeded;
     * notification outcome is shown as success or a non-blocking warning.
     */
    fun showToast(context: Context, result: NotificationSendResult, primarySaveSucceeded: Boolean = false) {
        val messageRes = when (result) {
            is NotificationSendResult.Success -> {
                if (primarySaveSucceeded) R.string.notification_send_success_after_save
                else R.string.notification_send_success
            }

            is NotificationSendResult.Failure -> {
                if (primarySaveSucceeded) R.string.notification_send_failed_after_save
                else failureMessageRes(result.reason)
            }
        }
        Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    private fun failureMessageRes(reason: NotificationSendFailureReason): Int = when (reason) {
        NotificationSendFailureReason.NOT_AUTHENTICATED ->
            R.string.notification_send_error_not_authenticated

        NotificationSendFailureReason.PERMISSION_DENIED ->
            R.string.notification_send_error_permission_denied

        NotificationSendFailureReason.NETWORK_ERROR ->
            R.string.notification_send_error_network

        NotificationSendFailureReason.CLOUD_FUNCTION_NOT_FOUND ->
            R.string.notification_send_error_function_not_found

        NotificationSendFailureReason.FCM_REJECTED ->
            R.string.notification_send_failed
    }
}
