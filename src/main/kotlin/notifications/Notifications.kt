package com.knifescout.notifications

import com.knifescout.csfloat.Entry
import com.knifescout.csfloat.getSecondCheapest
import com.knifescout.jedisPool
import com.knifescout.json
import com.knifescout.skinsnipe.getCheapest

val notified = mutableListOf<String>()

suspend fun notify(entries: List<Entry>) {
    val filteredEntries = entries.filter {
        it.discountPercentage >= 5 &&
        it.tradeVolume >= 20 &&
        !it.name.contains("Case Hardened") &&
        !it.name.contains("Shadow Daggers") &&
        !notified.contains("${it.id}${it.price}")
    }

    if (filteredEntries.isEmpty()) return

    val goodEntries = mutableListOf<Entry>()

    for (entry in filteredEntries) {
        try {
            var cachedResponse = jedisPool.get("compare:${entry.name}")
            val cheapest = if (cachedResponse == null) getCheapest(entry.name) else json.decodeFromString(cachedResponse)

            if (cheapest.price < entry.price) continue

            cachedResponse = jedisPool.get("secondcheapest:$entry.name")
            val secondCheapest = if (cachedResponse == null) getSecondCheapest(entry.name, entry.defIndex, entry.paintIndex) else json.decodeFromString(cachedResponse)

            if (secondCheapest == null) continue;
            if (secondCheapest.price < entry.price * 1.05f) continue

            goodEntries.add(entry)
        }
        catch (_: Exception) {
            continue
        }
    }

    if (goodEntries.isEmpty()) return

    val message = goodEntries.joinToString("\n") { "${it.name} - $${it.price}" }
    notified.addAll(goodEntries.map { "${it.id}${it.price}" })
    sendDiscordDM(message)
}