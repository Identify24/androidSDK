package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FlashToggle(
    val action: String = "toggleFlash",
    val result: Boolean
)