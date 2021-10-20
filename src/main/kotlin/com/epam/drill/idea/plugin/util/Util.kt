package com.epam.drill.idea.plugin.util

import com.epam.drill.plugins.test2code.api.*
import com.intellij.openapi.project.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*

val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

infix fun <T> KSerializer<T>.parse(rawData: String) = json.decodeFromString(this, rawData)

infix fun <T> KSerializer<T>.stringify(rawData: T) = json.encodeToString(this, rawData)

internal fun getProjectPath() = ProjectManager.getInstance().openProjects.first().projectFilePath?.split(
    Project.DIRECTORY_STORE_FOLDER
)?.get(0) ?: "Please, specify the file path"


internal fun CloseableHttpClient.getCoverageFromTest2code(
    token: String,
    url: String,
    agentId: String,
    buildVersion: String,
): CloseableHttpResponse {
    val httpPost = HttpPost("$url/api/agents/$agentId/plugins/test2code/dispatch-action").apply {
        entity = StringEntity(Action.serializer() stringify ExportCoverage(BuildPayload(buildVersion)))
        setHeader("Accept", "application/octet-stream")
        setHeader("Content-type", "application/octet-stream")
        setHeader("Authorization", "Bearer $token")
    }
    return execute(httpPost)
}

internal fun CloseableHttpClient.getToken(url: String): String {
    val httpPost = HttpPost("$url/api/login").apply {
        entity = StringEntity("{\"name\":\"guest\",\"password\":\"\"}")
        setHeader("Accept", "application/json")
        setHeader("Content-type", "application/json")
    }
    val response = execute(httpPost)
    return response.getHeaders("Authorization").first().value
}

internal fun CloseableHttpClient.getAgentIds(url: String): Set<String> {
    val httpGet = HttpGet("$url/api/version").apply {
        setHeader("Accept", "application/json")
        setHeader("Content-type", "application/json")
    }
    return Json.parseToJsonElement(String(execute(httpGet).entity.content.readBytes())).jsonObject["agents"]?.jsonArray?.mapNotNull {
        it.jsonObject["id"]?.jsonPrimitive?.content?.split("/")?.get(0)
    }?.toSet() ?: emptySet()
}

internal fun CloseableHttpClient.getBuildVersions(url: String, agentId: String): Set<String> {
    val httpGet = HttpGet("$url/api/agents/$agentId/plugins/test2code/builds/summary").apply {
        setHeader("Accept", "application/json")
        setHeader("Content-type", "application/json")
    }
    return Json.parseToJsonElement(String(execute(httpGet).entity.content.readBytes())).jsonArray.mapNotNull {
        it.jsonObject["buildVersion"]?.jsonPrimitive?.content?.split("/")?.get(0)
    }.toSet()
}
