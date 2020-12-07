package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Candidate(
    val candidate: String?,
    val sdpMid : String?,
    val sdpMLineIndex: Int?
)