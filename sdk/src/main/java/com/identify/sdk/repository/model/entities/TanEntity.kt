package com.identify.sdk.repository.model.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TanEntity(
    val id: String?
)