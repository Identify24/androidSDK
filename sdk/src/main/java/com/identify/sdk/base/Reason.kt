package com.identify.sdk.base

import androidx.annotation.StringRes
import com.identify.sdk.R

sealed class Reason(@StringRes val messageRes: Int)

//region Subclasses
class SendSuccess : Reason(R.string.reason_success_socket_data_send)
class NetworkError : Reason(R.string.reason_network)
class EmptyResultError : Reason(R.string.reason_empty_body)
class AuthenticationError : Reason(R.string.reason_authentication_error)
class GenericError : Reason(R.string.reason_generic)
class ResponseError : Reason(R.string.reason_response)
class TimeoutError : Reason(R.string.reason_timeout)
class NfcError : Reason(R.string.nfc_toast_message)
class ApiError(val message : List<String>?) : Reason(R.string.reason_response)
class SocketConnectionError : Reason(R.string.reason_socket_connection)
class SocketConnectionBackgroundError : Reason(R.string.reason_socket_connection)
class PersistenceEmpty : Reason(R.string.reason_persistance_empty)
class NoNetworkPersistenceEmpty : Reason(R.string.reason_no_network_persistance_empty)