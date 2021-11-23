package com.epam.drill.idea.plugin.tools

import com.epam.drill.idea.plugin.util.*
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
    private val file: File,
) {
    fun retrieveFile(): FileRetrieveStatus {
        runCatching {
            if (!file.createNewFile() && !file.exists()) {
                return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_CREATE_FILE,
                    additionalMessage = file.absolutePath)
            }
            HttpClients.createDefault().use { client ->
                val token = client.getToken(url)
                client.getAgentIds(url).takeIf {
                    it.contains(agentId)
                } ?: return setNotificationStatus(
                    status = FileRetrieveStatus.AGENT_NOT_FOUND,
                    additionalMessage = agentId)
                client.getBuildVersions(url, agentId).takeIf {
                    it.contains(buildVersion)
                } ?: return setNotificationStatus(
                    status = FileRetrieveStatus.BUILD_NOT_FOUND,
                    additionalMessage = buildVersion
                )
                val response = client.getCoverageFromTest2code(token, url, agentId, buildVersion)
                val inputStream = response.entity.content
                FileUtils.copyInputStreamToFile(inputStream, file)
            }
        }.onFailure {
            if (file.exists()) {
                file.delete()
            }
            return when (it) {
                is MalformedURLException -> {
                    setNotificationStatus(status = FileRetrieveStatus.REMOTE_URL_NOT_FOUND,
                        additionalMessage = url)
                }
                is IOException -> {
                    val message = "${it.message}.\nurl: '$url', agentId: '$agentId', fileDirectory: '${this.file}'"
                    setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_GET_FILE, message = message)
                }
                else -> setNotificationStatus(status = FileRetrieveStatus.UNKNOWN_EXCEPTION,
                    additionalMessage = it.message)
            }
        }

        return setNotificationStatus(status = FileRetrieveStatus.SUCCESS, additionalMessage = file.absolutePath)
    }

}
