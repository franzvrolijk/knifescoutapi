package com.knifescout.notifications

import com.knifescout.json
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

suspend fun sendDiscordDM(content: String) {
    val botToken = System.getenv("BOT_TOKEN")
    val discordUid = System.getenv("DISCORD_UID")

    val client = HttpClient(CIO)

    var body = mapOf("recipient_id" to discordUid)

    var response = client.post("https://discord.com/api/v10/users/@me/channels") {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bot $botToken")
        setBody(json.encodeToString(body))
    }

    if (!response.status.isSuccess())
    {
        client.close()
        throw Exception("Discord channel HTTP status code ${response.status}")
    }

    val resBody = response.bodyAsText()

    val dmResponse: DmResponse = json.decodeFromString(resBody)

    val url = "https://discord.com/api/v10/channels/${dmResponse.id}/messages"

    body  = mapOf("content" to content)

    response = client.post(url) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bot $botToken")
        setBody(json.encodeToString(body))
    }

    client.close()

    if (!response.status.isSuccess()){
        throw Exception("Discord DM HTTP status code ${response.status}")
    }
}

@Serializable
data class DmResponse(
    val id: String,
    val type: Int
)