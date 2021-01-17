package com.identify.sdk.repository.model.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SocketResponse(
   val action: String?,
   val content: String? = null,
   val location : String? =  null,
   val room : String? = null,
   val msg : String? = null,
   val sdp : Sdp? = null,
   val candidate: Candidate? = null,
   @Json(name = "is_admin")
   val isAdmin: Boolean ?= null,
   @Json(name = "tid")
   val tId: Int ?= null

)