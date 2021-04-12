package com.github.evgeniikuznetsov.drill4jplugin.tools

import kotlinx.serialization.json.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*
import java.io.*
import java.net.*
import java.util.*

class UrlFileRetriever(var url: String, var agentId: String, var fileDirectory: String) {
    @OptIn(ExperimentalStdlibApi::class)
    fun retrieveFile(): FileRetrieveStatus {
        val file = File(fileDirectory)
        try {
            if (!file.createNewFile() && !file.exists()) {
                return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_CREATE_FILE,
                    additionalMessage = fileDirectory)
            }

            val client = HttpClients.createDefault()
            val httpPost = HttpPost("$url/api/login")
            httpPost.entity = StringEntity("{\"name\":\"guest\",\"password\":\"\"}");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            val response = client.execute(httpPost)
            val auth = response.getHeaders("Authorization").first()

            val httpPost2 = HttpPost("$url/api/agents/$agentId/plugins/test2code/dispatch-action")
            httpPost2.entity = StringEntity("{\"type\":\"EXPORT_COVERAGE\"}");
            httpPost2.setHeader("Accept", "application/json")
            httpPost2.setHeader("Content-type", "application/json")
            httpPost2.setHeader("Authorization", "Bearer ${auth.value}")
            val response2 = client.execute(httpPost2)
            val readAllBytes = response2.entity.content.readBytes()
            file.writeBytes(Base64.getDecoder()
                .decode(Json.parseToJsonElement(readAllBytes.decodeToString()).jsonObject.get("message")!!.jsonPrimitive.content))
            client.close()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            if (file.exists()) {
                file.delete()
            }
            return setNotificationStatus(status = FileRetrieveStatus.REMOTE_URL_NOT_FOUND, additionalMessage = url)
        } catch (e: IOException) {
            return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_GET_FILE, message = e.message)
        }

        return setNotificationStatus(status = FileRetrieveStatus.SUCCESS, additionalMessage = fileDirectory)
    }

}
