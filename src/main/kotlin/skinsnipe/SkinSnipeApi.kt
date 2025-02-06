package com.knifescout.skinsnipe

import com.knifescout.json
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable

suspend fun skinSnipeRequest(url: String): HttpResponse {
    val client = HttpClient(CIO)

    val response = client.request(url) {
        method = HttpMethod.Get
        headers {
            append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0) Gecko/20100101 Firefox/135.0")
            append("Accept", "application/json, text/plain, */*")
            append("Accept-Language", "en-US,en;q=0.5")
            append("Origin", "https://www.skinsnipe.com")
            append("Referer", "https://www.skinsnipe.com/")
            append("DNT", "1")
            append("Connection", "keep-alive")
            append("Sec-Fetch-Dest", "empty")
            append("Sec-Fetch-Mode", "cors")
            append("Sec-Fetch-Site", "cross-site")
            append("Pragma", "no-cache")
            append("Cache-Control", "no-cache")
        }
    }

    client.close()

    return response
}

@Serializable
data class CheapestResult (
    val price: Float,
    val marketName: String
)

suspend fun getCheapest(name: String): CheapestResult {
    val id = getSkinSnipeId(name)

    val exterior = when {
        name.contains("Factory", ignoreCase = true) -> 1
        name.contains("Minimal", ignoreCase = true) -> 2
        name.contains("Field", ignoreCase = true) -> 3
        name.contains("Well", ignoreCase = true) -> 4
        name.contains("Battle", ignoreCase = true) -> 5
        else -> throw Exception("Coult not find wear in $name")
    }

    val url = "	https://pricing.tradeupspy.com/compare/buy?id=$id&extra=1&exterior=$exterior&range=7d"

    val response = skinSnipeRequest(url)

    if (!response.status.isSuccess()) {
        throw Exception("Non-OK code from SkinSnipe search for $name")
    }

    val body = response.bodyAsText()

    val result = json.decodeFromString<SkinSnipePrices>(body)

    if (result.prices.isEmpty()) {
        throw Exception("No prices found")
    }

    val cheapest = result.prices
        .filter{ it.value != null }
        .minBy { it.value!! }

    return CheapestResult(cheapest.value!!, cheapest.marketName ?: "Unknown")
}

suspend fun getSkinSnipeId(name: String): Int {
    val formattedName = name.replace("\\(.*?\\)".toRegex(), "").trim().encodeURLQueryComponent()
    val url = "https://pricing.tradeupspy.com/skins/search?query=$formattedName&cat=-1&game=730"

    val response = skinSnipeRequest(url)

    if (!response.status.isSuccess()) {
        throw Exception("Non-OK code from SkinSnipe search: ${response.status}, name: ${formattedName}")
    }

    val body = response.bodyAsText()

    val results = json.decodeFromString<List<SkinSnipeSearch>>(body)

    return results.first().id
}

@Serializable
data class SkinSnipeSearch (
    val id: Int,
    val name: String,
)

@Serializable
data class SkinSnipePrices (
    val skin: SkinSnipeSkin,
    val prices: List<SkinSnipePrice>
)

@Serializable
data class SkinSnipeSkin (
    val id: Int
)

@Serializable
data class SkinSnipePrice (
    val marketId: Int?,
    val marketName: String?,
    val value: Float? // dollars
)
