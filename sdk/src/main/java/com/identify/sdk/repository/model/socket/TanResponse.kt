package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TanResponse(
    val action : String = "tan_entered",
    val room : String,
    val tid : String,
    val tan: String
)