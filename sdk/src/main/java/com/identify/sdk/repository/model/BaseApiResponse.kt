package com.identify.sdk.repository.model

data class BaseApiResponse<T>(
    val result : Boolean ?,
    val messages : List<String>?,
    val data : T?
)