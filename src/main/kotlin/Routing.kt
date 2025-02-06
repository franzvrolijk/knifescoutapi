package com.knifescout

import com.knifescout.csfloat.Entry
import com.knifescout.csfloat.getSecondCheapest
import com.knifescout.csfloat.getMostDiscounted
import com.knifescout.discord.sendDiscordDM
import com.knifescout.skinsnipe.CheapestResult
import com.knifescout.skinsnipe.getCheapest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
}

fun Application.configureRouting() {
    val scope = CoroutineScope(Dispatchers.Default)

    routing {
        get("prewarm") {
            scope.launch {
                val entries = getMostDiscounted() ?: return@launch
                jedisPool.setex("baseprice", 60 * 15, json.encodeToString(entries))

                val potentialEntries = entries.filter { it.discountPercentage >= 5 && it.tradeVolume >= 20 && !it.name.contains("Case Hardened") && !it.name.contains("Shadow Daggers") }
                if (potentialEntries.isEmpty()) return@launch

                val goodEntries = mutableListOf<Entry>()

                for (entry in potentialEntries) {
                    var cachedResponse = jedisPool.get("compare:${entry.name}")
                    val cheapest = if (cachedResponse == null) getCheapest(entry.name) else json.decodeFromString(cachedResponse)

                    if (cheapest.price < entry.price) continue

                    cachedResponse = jedisPool.get("secondcheapest:$entry.name")
                    val secondCheapest = if (cachedResponse == null) getSecondCheapest(entry.name, entry.defIndex, entry.paintIndex) else json.decodeFromString(cachedResponse)

                    if (secondCheapest == null) continue;
                    if (secondCheapest.price < entry.price * 1.05f) continue

                    goodEntries.add(entry)
                }

                if (goodEntries.isEmpty()) return@launch

                val message = goodEntries.joinToString("\n") { "${it.name} - $${it.price}" }
                sendDiscordDM(message)
            }

            call.respond(HttpStatusCode.OK)
            return@get
        }

        get("api/csfloat/baseprice") {
            val cachedResponse = jedisPool.get("baseprice")
            if (cachedResponse != null) {
                call.respondText(cachedResponse)
                return@get
            }

            val entries = getMostDiscounted()

            if (entries == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data")
                return@get
            }

            val strigifiedEntries = json.encodeToString(entries)

            jedisPool.setex("baseprice", 60 * 15, strigifiedEntries)

            call.respond(strigifiedEntries)
            return@get
        }

        get("api/cheapest/{name}") {
            val name = call.parameters["name"]
                ?: return@get call.respondText("Missing name", status = HttpStatusCode.BadRequest)

            val cachedResponse = jedisPool.get("compare:$name")
            if (cachedResponse != null) {
                call.respondText(cachedResponse)
                return@get
            }

            val cheapest = getCheapest(name)

            val stringifiedEntry = json.encodeToString(cheapest)

            jedisPool.setex("compare:$name", 60 * 5, stringifiedEntry)

            call.respond(stringifiedEntry)
            return@get
        }

        get("api/csfloat/secondcheapest/{name}/{id}/{paintIndex}") {
            val name = call.parameters["name"]
                ?: return@get call.respondText("Missing name", status = HttpStatusCode.BadRequest)

            val idString = call.parameters["id"]
                ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)

            val id = idString.toIntOrNull()
                ?: return@get call.respondText("Invalid id", status = HttpStatusCode.BadRequest)

            val paintIndexString = call.parameters["paintIndex"]
                ?: return@get call.respondText("Missing paintIndex", status = HttpStatusCode.BadRequest)

            val paintIndex = paintIndexString.toIntOrNull()
                ?: return@get call.respondText("Invalid paintIndex", status = HttpStatusCode.BadRequest)

            val cachedResponse = jedisPool.get("secondcheapest:$name")
            if (cachedResponse != null) {
                call.respondText(cachedResponse)
                return@get
            }

            val secondCheapest = getSecondCheapest(name, id, paintIndex)

            if (secondCheapest == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data")
                return@get
            }

            val stringifiedEntry = json.encodeToString(secondCheapest)

            jedisPool.setex("secondcheapest:$name", 60 * 5, stringifiedEntry)

            call.respond(stringifiedEntry)
            return@get
        }

        get("health") {
            call.respondText { json.encodeToString(jedisPool.keys("*")) }
        }
    }
}
