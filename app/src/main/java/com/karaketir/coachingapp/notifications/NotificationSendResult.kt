package com.karaketir.coachingapp.notifications

sealed class NotificationSendResult {
    data object Success : NotificationSendResult()

    data class Failure(val reason: NotificationSendFailureReason) : NotificationSendResult()
}

enum class NotificationSendFailureReason {
    NOT_AUTHENTICATED,
    PERMISSION_DENIED,

    /** Device offline, timeout, or Functions unreachable. */
    NETWORK_ERROR,

    /** Callable not deployed or wrong Firebase project. */
    CLOUD_FUNCTION_NOT_FOUND,

    FCM_REJECTED,
}
