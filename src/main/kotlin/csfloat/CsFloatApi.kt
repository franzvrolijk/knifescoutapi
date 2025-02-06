package com.knifescout.csfloat

import com.knifescout.json
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay

suspend fun getSecondCheapest(name: String, id: Int, paintIndex: Int): Entry? {
    val params = "?category=1&type=buy_now&sort_by=lowest_price&limit=2&def_index=$id&paint_index=$paintIndex"

    val minFloat = when {
        name.contains("Factory", ignoreCase = true) -> 0
        name.contains("Minimal", ignoreCase = true) -> 0.07
        name.contains("Field", ignoreCase = true) -> 0.15
        name.contains("Well", ignoreCase = true) -> 0.37
        name.contains("Battle", ignoreCase = true) -> 0.45
        else -> throw Exception("Could not find wear in $name")
    }

    val maxFloat = when {
        name.contains("Factory", ignoreCase = true) -> 0.07
        name.contains("Minimal", ignoreCase = true) -> 0.15
        name.contains("Field", ignoreCase = true) -> 0.37
        name.contains("Well", ignoreCase = true) -> 0.45
        name.contains("Battle", ignoreCase = true) -> 1
        else -> throw Exception("Could not find wear in $name")
    }


    val body = getCsFloatData("$params&min_float=$minFloat&max_float=$maxFloat") ?: return null

    val csFloatResponse: CsFloatResponse = json.decodeFromString(body)

    if (csFloatResponse.data.count() != 2) {
        return null
    }

    val entries = csFloatResponse.data.map { toEntry(it) }.sortedBy { it.price }

    return entries.last()
}

suspend fun getMostDiscounted(): List<Entry>? {
    val params = "?limit=50&rarity=6&sort_by=highest_discount&category=1&max_float=0.38&min_price=7248&max_price=50000&type=buy_now&def_index=500,503,505,506,507,508,509,512,514,515,516,517,518,519,520,521,522,523,525,526"

    var body: String = getCsFloatData(params) ?: return null

    var csFloatResponse: CsFloatResponse = json.decodeFromString(body)

    var allResults = csFloatResponse.data.toList()

    var i = 0

    while (i < 7) {
        delay(500)

        body = getCsFloatData("$params&cursor=${csFloatResponse.cursor}") ?: return null

        csFloatResponse = json.decodeFromString(body)
        allResults = allResults.plus(csFloatResponse.data)
        i++
    }

    val entries = allResults
        .filter { it.state == "listed"}
        .map { toEntry(it) }
        .filter { it.discountPercentage > 0 }
        .sortedByDescending { it.discountPercentage }

    return entries
}

suspend fun getCsFloatData(params: String): String? {
    val baseUrl = "https://csfloat.com/api/v1/listings"

    val client = HttpClient(CIO)

    val response = client.request("$baseUrl$params") {
        method = HttpMethod.Get
        headers {
            append("Authorization", System.getenv("CSFLOAT_API_KEY"))
        }
    }

    if (!response.status.isSuccess()) {
        client.close()
        return null
    }

    val body = response.bodyAsText()

    client.close()

    return body
}