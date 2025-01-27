package com.knifescout

import com.knifescout.responses.CsFloatResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import redis.clients.jedis.UnifiedJedis

private val json = Json {
    ignoreUnknownKeys = true
}

suspend fun getCsFloatData(url: String): String? {
    val redisHost = System.getenv("REDIS_HOST")
    val redisPort = 6379
    val redisConnectionString = "redis://$redisHost:$redisPort"

    val jedis = UnifiedJedis(redisConnectionString)
    
    val cachedValue = jedis.get(url);

    if (cachedValue != null) {
        jedis.close()
        return cachedValue
    }

    val client = HttpClient(CIO)

    val response = client.request(url) {
        method = HttpMethod.Get;
        headers {
            append("Authorization", "eGTTci1Zr7mmgR3ZXoi7M943snhmVvR4")
        }
    }

    if (!response.status.isSuccess()) {
        client.close()
        jedis.close()
        return null
    }

    val body = response.bodyAsText()

    jedis.setex(url, 60 * 30, body)
    client.close()
    jedis.close()

    return body
}

fun Application.configureRouting() {
    routing {
        get("api/csfloat/baseprice") {
            val url = "https://csfloat.com/api/v1/listings?limit=50&rarity=6&sort_by=highest_discount&max_float=0.38&min_price=7248&max_price=10000000&type=buy_now";

            val body = getCsFloatData(url)

            if (body == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            val csFloatResponse: CsFloatResponse = json.decodeFromString(body)

            val entries = csFloatResponse.data
                .map { toEntry(it) }
                .sortedByDescending { it.discountPercentage }

            call.respond(json.encodeToString(entries));

            return@get;
        }

        get("api/csfloat/secondcheapest/{name}") {
            val name = call.parameters["name"]
                ?: return@get call.respondText("Missing name", status = HttpStatusCode.BadRequest);

            val url = "https://csfloat.com/api/v1/listings?category=1&type=buy_now&sort_by=lowest_price&limit=2&market_hash_name=$name";

            val body = getCsFloatData(url)

            if (body == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching CsFloat data");
                return@get;
            }

            val csFloatResponse: CsFloatResponse = json.decodeFromString(body)

            if (csFloatResponse.data.count() != 2)
                return@get call.respondText("Bad CsFloat response", status = HttpStatusCode.NotFound)

            val entries = csFloatResponse.data.map { toEntry(it) }.sortedBy { it.price }

            call.respond(json.encodeToString(entries.last()));

            return@get;
        }

        get("/health") {
            val redisHost = System.getenv("REDIS_HOST")
            val redisPort = 6379
            val redisConnectionString = "redis://$redisHost:$redisPort"

            val jedis = UnifiedJedis(redisConnectionString)

            val keys = jedis.keys("*").toList()

            if (keys.isEmpty()) call.respond("No keys")
            else call.respond(json.encodeToString(keys))

            return@get;
        }
    }
}
