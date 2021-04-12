package com.github.evgeniikuznetsov.drill4jplugin.tools

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.util.*

class UrlFileRetriever (var url: String, var fileDirectory: String){
    fun retrieveFile() : FileRetrieveStatus {
        val file = File(fileDirectory)
        try {
            if (!file.createNewFile() && !file.exists()) {
                return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_CREATE_FILE, additionalMessage = fileDirectory)
            }

            val client = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }

            var message: String = ""
            runBlocking {
                val response: String? = client.request<HttpResponse>("http://ecse005009ba.epam.com:8090/api/login") {
                    method = HttpMethod.Post
                    body = TextContent("{\"name\": \"guest\",\"password\": \"\" }", ContentType.Text.Plain)
                }.headers["Authorization"]

                message = client.request<Message>(url) {
                    method = HttpMethod.Post
                    header("Authorization", "Bearer $response")
                    body = TextContent("{\"type\": \"EXPORT_COVERAGE\"}", ContentType.Text.Plain)
                }.message
            }
            file.writeBytes(Base64.getDecoder().decode(message))
        }
        catch (e : MalformedURLException) {
            e.printStackTrace()
            if (file.exists()) {
                file.delete()
            }
            return setNotificationStatus(status = FileRetrieveStatus.REMOTE_URL_NOT_FOUND, additionalMessage = url)
        }
        catch (e : IOException) {
            return setNotificationStatus(status = FileRetrieveStatus.CAN_NOT_GET_FILE, message = e.message)
        }

        return setNotificationStatus(status = FileRetrieveStatus.SUCCESS, additionalMessage = fileDirectory)
    }

    private fun InputStream.writeToFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

}

@Serializable
data class Message(val code: String, val message: String)
