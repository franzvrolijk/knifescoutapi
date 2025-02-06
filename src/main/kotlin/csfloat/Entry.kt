package com.knifescout.csfloat

import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val name: String,
    val price: Float,
    val basePrice: Float,
    val iconUrl: String,
    val discountPercentage: Float,
    val id: String,
    val tradeVolume: Int,
    val defIndex: Int,
    val paintIndex: Int
) {
    constructor(csFloatEntry: CsFloatEntry, discountPercentage: Float) : this(
        name = csFloatEntry.item.name,
        price = csFloatEntry.priceInDollars,
        basePrice = csFloatEntry.reference.basePriceInDollars,
        iconUrl = csFloatEntry.item.fullIconUrl,
        discountPercentage = discountPercentage,
        id = csFloatEntry.id,
        tradeVolume = csFloatEntry.reference.quantity,
        defIndex = csFloatEntry.item.def_index,
        paintIndex = csFloatEntry.item.paint_index
    )
}

fun toEntry(csFloatEntry: CsFloatEntry): Entry {
    val discountPercentage = 100 - (csFloatEntry.priceInDollars / csFloatEntry.reference.basePriceInDollars * 100)
    return Entry(csFloatEntry, discountPercentage)
}