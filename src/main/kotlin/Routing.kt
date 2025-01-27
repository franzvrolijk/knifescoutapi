package com.knifescout

import com.knifescout.csfloat.getMostDiscounted
import com.knifescout.csfloat.getSecondCheapest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
}

fun Application.configureRouting() {
    routing {
        get("api/csfloat/baseprice") {
            val cachedResponse = jedisPool.get("baseprice")
            if (cachedResponse != null) {
                call.respondText(cachedResponse);
                return@get;
            }

            val entries = getMostDiscounted()

            if (entries == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            val strigifiedEntries = json.encodeToString(entries);

            jedisPool.setex("baseprice", 60 * 15, strigifiedEntries)

            call.respond(strigifiedEntries);
            return@get;
        }

        get("api/csfloat/secondcheapest/{name}") {
            val name = call.parameters["name"]
                ?: return@get call.respondText("Missing name", status = HttpStatusCode.BadRequest);

            val cachedResponse = jedisPool.get("secondcheapest:$name")
            if (cachedResponse != null) {
                call.respondText(cachedResponse);
                return@get;
            }

            val secondCheapest = getSecondCheapest(name)

            if (secondCheapest == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            val stringifiedEntry = json.encodeToString(secondCheapest);

            jedisPool.setex("secondcheapest:$name", 60 * 5, stringifiedEntry)

            call.respond(stringifiedEntry);
            return@get;
        }

        get("health") {
            call.respondText { json.encodeToString(jedisPool.keys("*")) }
        }
    }
}
