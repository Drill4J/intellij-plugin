package com.epam.drill.idea.plugin.tools

import com.intellij.notification.*

enum class FileRetrieveStatus(
    var message: String,
    var notificationType: NotificationType,
    var additionalMessage: String?,
) {

    SUCCESS("File success retrieve to directory %s", NotificationType.INFORMATION, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    },
    REMOTE_URL_NOT_FOUND("No connection to %s", NotificationType.ERROR, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    },
    AGENT_NOT_FOUND("Agent %s not found", NotificationType.ERROR, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    },
    BUILD_NOT_FOUND("Build %s not found", NotificationType.ERROR, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    },
    CAN_NOT_CREATE_FILE("Can not create file to directory %s", NotificationType.ERROR, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    },
    CAN_NOT_GET_FILE("Can not get file. Please check setting or url", NotificationType.ERROR, null) {
        override fun message(): String {
            return message
        }
    },
    CAN_NOT_FIND_FILE("Can not find the file. Please check local file path", NotificationType.ERROR, null) {
        override fun message(): String {
            return message
        }
    },
    UNKNOWN_EXCEPTION("Exception: ", NotificationType.ERROR, null) {
        override fun message(): String {
            return String.format(message, additionalMessage)
        }
    };

    abstract fun message(): String
}

fun setNotificationStatus(
    status: FileRetrieveStatus,
    message: String? = null,
    additionalMessage: String? = null,
): FileRetrieveStatus {
    return status.also {
        it.message = message ?: it.message
        it.additionalMessage = additionalMessage ?: it.additionalMessage
    }
}
