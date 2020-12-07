package com.identify.sdk.repository.model.socket

import com.squareup.moshi.JsonClass
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class DataChannelMessage(val type: String, val data: JSONObject) {

    companion object {

        const val KEY_TYPE = "type"
        const val KEY_DATA = "data"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_MESSAGE = "message"

        const val TYPE_CHAT_MESSAGE = "chatmessage"
        const val TYPE_LOCATION = "location"

        fun createLocationMessage(latitude: Double, longitude: Double): JSONObject = JSONObject().apply {
            put(KEY_TYPE, DataChannelMessage.TYPE_LOCATION)
            put(KEY_DATA, JSONObject().apply {
                put(KEY_LATITUDE, latitude)
                put(KEY_LONGITUDE, longitude)
            })
        }

        fun createChatMessage(message: String): JSONObject = JSONObject().apply {
            put(KEY_TYPE, DataChannelMessage.TYPE_CHAT_MESSAGE)
            put(KEY_DATA, JSONObject().apply {
                put(KEY_MESSAGE, message)
            })
        }
    }
}