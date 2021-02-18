package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NfcStatus(
    val action: String = "NFCStatus",
    val room: String,
    val status: String
)