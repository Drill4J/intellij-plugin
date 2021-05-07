package com.github.evgeniikuznetsov.drill4jplugin.tools

import com.github.evgeniikuznetsov.drill4jplugin.util.*
import com.intellij.openapi.diagnostic.*
import org.apache.commons.io.*
import org.apache.http.impl.client.*
import java.io.*
import java.net.*


private val logger: Logger = Logger.getInstance(UrlFileRetriever::class.java)

class UrlFileRetriever(
    private val url: String,
    private val agentId: String,
    private val buildVersion: String,
    private val fileDirectory: String,
) {
    fun retrieveFile(): FileRetrieveStatus {
        val file = File(fileDirectory)
        try {
            if (!file.createNewFile() && !file.exists()) {
                return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_CREATE_FILE,
                    additionalMessage = fileDirectory)
            }
            HttpClients.createDefault().use { client ->
                val token = client.getToken(url)
                val response = client.getCoverageFromTest2code(token, url, agentId, buildVersion)
                val inputStream = response.entity.content
                FileUtils.copyInputStreamToFile(inputStream, file)
            }

        } catch (e: MalformedURLException) {
            e.printStackTrace()
            if (file.exists()) {
                file.delete()
            }
            return setNotificationStatus(status = FileRetrieveStatus.REMOTE_URL_NOT_FOUND, additionalMessage = url)
        } catch (e: IOException) {
            val message = "${e.message}.\nurl: '$url', agentId: '$agentId', fileDirectory: '$fileDirectory'"
            return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_GET_FILE, message = message)
        }

        return setNotificationStatus(status = FileRetrieveStatus.SUCCESS, additionalMessage = fileDirectory)
    }

}
