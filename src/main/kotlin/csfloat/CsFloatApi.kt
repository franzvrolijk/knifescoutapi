package com.knifescout.csfloat

import com.knifescout.jedisPool
import com.knifescout.json
import com.knifescout.responses.CsFloatResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun getSecondCheapest(name: String): Entry? {
    val params = "?category=1&type=buy_now&sort_by=lowest_price&limit=2&market_hash_name=$name";

    val body = getCsFloatData(params)

    if (body == null) {
        return null
    }

    val csFloatResponse: CsFloatResponse = json.decodeFromString(body)

    if (csFloatResponse.data.count() != 2) {
        return null
    }

    val entries = csFloatResponse.data.map { toEntry(it) }.sortedBy { it.price }

    return entries.last()
}

suspend fun getMostDiscounted(): List<Entry>? {
    val params = "?limit=50&rarity=6&sort_by=highest_discount&max_float=0.38&min_price=7248&max_price=10000000&type=buy_now";

    val body = getCsFloatData(params)

    if (body == null) {
        return null
    }

    val csFloatResponse: CsFloatResponse = json.decodeFromString(body)

    val entries = csFloatResponse.data
        .map { toEntry(it) }
        .sortedByDescending { it.discountPercentage }

    return entries
}

suspend fun getCsFloatData(params: String): String? {
    val baseUrl = "https://csfloat.com/api/v1/listings"

    val cachedValue = jedisPool.get("$baseUrl$params");

    if (cachedValue != null) {
        return cachedValue
    }

    val client = HttpClient(CIO)

    val response = client.request("$baseUrl$params") {
        method = HttpMethod.Get;
        headers {
            append("Authorization", System.getenv("CSFLOAT_API_KEY"))
        }
    }

    if (!response.status.isSuccess()) {
        client.close()
        return null
    }

    val body = response.bodyAsText()

    jedisPool.setex("$baseUrl$params", 60 * 30, body)

    client.close()

    return body
}