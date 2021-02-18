package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResultToogle(
    val room: String,
    val action: String?,
    val result: Boolean
)