package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class SocketSubscribe(
    val action: String = "subscribe",
    val room: String,
    val location: String = "conf"
)