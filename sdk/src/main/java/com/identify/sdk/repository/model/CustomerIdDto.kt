package com.identify.sdk.repository.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerIdDto(
    val identId: String
) : Parcelable