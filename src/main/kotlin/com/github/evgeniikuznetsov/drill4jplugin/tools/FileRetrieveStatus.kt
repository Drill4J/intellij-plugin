package com.github.evgeniikuznetsov.drill4jplugin.tools

import com.intellij.notification.*
import org.apache.commons.lang3.*

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
    };

    abstract fun message(): String
}

fun setNotificationStatus(
    status: FileRetrieveStatus,
    message: String? = null,
    additionalMessage: String? = null,
): FileRetrieveStatus {
    return status.also {
        it.message = ObjectUtils.defaultIfNull(message, it.message)
        it.additionalMessage = ObjectUtils.defaultIfNull(additionalMessage, it.additionalMessage)
    }
}
