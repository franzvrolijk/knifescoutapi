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
            val entries = getMostDiscounted()

            if (entries == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            call.respond(json.encodeToString(entries));

            return@get;
        }

        get("api/csfloat/secondcheapest/{name}") {
            val name = call.parameters["name"]
                ?: return@get call.respondText("Missing name", status = HttpStatusCode.BadRequest);

            val secondCheapest = getSecondCheapest(name)

            if (secondCheapest == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            call.respond(json.encodeToString(secondCheapest));

            return@get;
        }

        get("health") {
            call.respondText { json.encodeToString(jedisPool.keys("*")) }
        }
    }
}
