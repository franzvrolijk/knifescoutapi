package com.knifescout.csfloat

import kotlinx.serialization.Serializable

@Serializable
data class CsFloatResponse(
    var data: List<CsFloatEntry>,
    val cursor: String,
)

@Serializable
data class CsFloatEntry(
    val item: CsFloatItem,
    val price: Float,
    val reference: CsFloatReference,
    val id: String,
    val state: String
) {
    val priceInDollars: Float
        get() = price / 100
}

@Serializable
data class CsFloatItem(
    val icon_url: String,
    val market_hash_name: String,
    val def_index: Int,
    val paint_index: Int
) {
    val name: String
        get() = market_hash_name

    val fullIconUrl: String
        get() = "https://community.cloudflare.steamstatic.com/economy/image/$icon_url"
}

@Serializable
data class CsFloatReference(
    val base_price: Float,
    val quantity: Int
) {
    val basePriceInDollars: Float
        get() = base_price / 100
}