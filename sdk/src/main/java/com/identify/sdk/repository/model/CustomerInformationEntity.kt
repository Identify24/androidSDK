package com.identify.sdk.repository.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CustomerInformationEntity(
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "created_by")
    val createdBy: String?,
    @Json(name = "customer_id")
    val customerId: String?,
    @Json(name = "customer_uid")
    val customerUid: String?,
    @Json(name = "form_uid")
    val formUid: String?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "status")
    val status: String?
) : Parcelable