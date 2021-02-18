package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallRejected(
    val action: String = "callRejected",
    val room: String,
    val msg: String = "Call rejected by Customer."
)