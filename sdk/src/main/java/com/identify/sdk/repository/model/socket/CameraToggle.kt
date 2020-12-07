package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CameraToggle(
    val action: String = "toggleCamera",
    val result: Boolean
)