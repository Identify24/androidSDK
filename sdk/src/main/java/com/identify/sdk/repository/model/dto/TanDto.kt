package com.identify.sdk.repository.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TanDto(
    var tid : String,
    var tan : String
)