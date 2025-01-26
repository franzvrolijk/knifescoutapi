package com.knifescout

import com.knifescout.responses.CsFloatEntry
import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val name: String,
    val price: Float,
    val basePrice: Float,
    val iconUrl: String,
    val discountPercentage: Float,
    val id: String,
    val tradeVolume: Int
) {
    constructor(csFloatEntry: CsFloatEntry, discountPercentage: Float) : this(
        name = csFloatEntry.item.name,
        price = csFloatEntry.priceInDollars,
        basePrice = csFloatEntry.reference.basePriceInDollars,
        iconUrl = csFloatEntry.item.fullIconUrl,
        discountPercentage = discountPercentage,
        id = csFloatEntry.id,
        tradeVolume = csFloatEntry.reference.quantity
    )
}

fun toEntry(csFloatEntry: CsFloatEntry): Entry {
    val discountPercentage = 100 - (csFloatEntry.priceInDollars / csFloatEntry.reference.basePriceInDollars * 100)
    return Entry(csFloatEntry, discountPercentage)
}