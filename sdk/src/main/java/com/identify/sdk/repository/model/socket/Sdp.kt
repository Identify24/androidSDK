package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Sdp(
    val type : String?,
    val sdp : String?
)