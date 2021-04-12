package com.github.evgeniikuznetsov.drill4jplugin.config.constants

import com.github.evgeniikuznetsov.drill4jplugin.config.SettingsState
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.FileExistsException
import java.io.IOException
import java.util.regex.Pattern

object Constants {
    val credentialAttribute
        get() = CredentialAttributes("JSP Reactive")
    val regexp: Pattern = Pattern.compile("^.*\\.jsp(f)?$")
    const val localClassPath = "WebStore"
}

enum class FtpTypes {
    FTP, FTPS, SFTP
}

fun validateFile(virtualFile: VirtualFile?): VirtualFile {
    when {
        virtualFile == null -> throw FileValidationException("No file for deploy", NotificationType.WARNING)
        !Constants.regexp.matcher(virtualFile.path).matches() -> throw FileValidationException("Invalid file format", NotificationType.ERROR)
        else -> return virtualFile
    }
}

fun sendFiles(project: Project, body: () -> Unit) {
    try {
        body()
    } catch (e: FileValidationException) {
        GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(e.message.toString(), e.type).notify(project)
        return
    } catch (e: IOException) {
        GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(e.localizedMessage, NotificationType.ERROR).notify(project)
    } catch (e: Exception) {
        GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(e.message.toString(), NotificationType.ERROR).notify(project)
        return
    }
}

fun createPath(path: String): String {
    return SettingsState.settings.remoteFilePath + path.substringAfter(Constants.localClassPath)
}

class FileValidationException(override val message: String?, notificationType: NotificationType) : FileExistsException() {
    val type: NotificationType = notificationType
}