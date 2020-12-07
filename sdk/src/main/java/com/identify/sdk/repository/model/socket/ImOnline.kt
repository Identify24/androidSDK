package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImOnline(
    val action: String = "imOnline",
    val room: String,
    val location: String = "conf"
)