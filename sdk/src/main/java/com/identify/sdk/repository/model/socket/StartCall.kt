package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class StartCall(
    val action: String = "startCall",
    val room: String
)