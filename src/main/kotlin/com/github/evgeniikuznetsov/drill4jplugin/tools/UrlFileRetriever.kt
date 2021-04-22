package com.github.evgeniikuznetsov.drill4jplugin.tools

import com.intellij.openapi.diagnostic.*
import org.apache.commons.io.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*
import java.io.*
import java.net.*


private val logger: Logger = Logger.getInstance(UrlFileRetriever::class.java)

class UrlFileRetriever(
    private val url: String,
    private val agentId: String,
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
                val token = client.getToken()
                val response = client.getCoverageFromTest2code(token)
                val inputStream = response.entity.content
                FileUtils.copyInputStreamToFile(inputStream, file);
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


    private fun CloseableHttpClient.getCoverageFromTest2code(
        token: String,
    ): CloseableHttpResponse {
        val httpPost = HttpPost("$url/api/agents/$agentId/plugins/test2code/dispatch-action").apply {
            entity = StringEntity("{\"type\":\"EXPORT_COVERAGE\"}")
            setHeader("Accept", "application/octet-stream")
            setHeader("Content-type", "application/octet-stream")
            setHeader("Authorization", "Bearer $token")
        }
        logger.info("http post for coverage: $httpPost")
        return execute(httpPost)
    }

    private fun CloseableHttpClient.getToken(): String {
        val httpPost = HttpPost("$url/api/login").apply {
            entity = StringEntity("{\"name\":\"guest\",\"password\":\"\"}")
            setHeader("Accept", "application/json")
            setHeader("Content-type", "application/json")
        }
        logger.info("http post for login: $httpPost")
        val response = execute(httpPost)
        return response.getHeaders("Authorization").first().value
    }

}
