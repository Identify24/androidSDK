package com.identify.sdk.repository.soket


import android.content.Context
import android.util.Log
import com.identify.sdk.base.*
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
import com.identify.sdk.repository.model.socket.*
import com.identify.sdk.repository.network.TIMEOUT_DURATION
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class SocketSource {

    //region Properties

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    private var context : Context ?= null

    var socketConnectionStatusListener : (socketConnectionStatus : SocketConnectionStatus) -> Unit = { _->}


    companion object{
        private val socketSource by lazy {
            SocketSource()
        }
        fun getInstance() : SocketSource =
            socketSource
    }



    interface SocketResponseListener{
        fun onSocketResponse(socketResponse: Result<SocketResponse>)
    }

    var socketResponseListener : SocketResponseListener ?= null

    var webSocket : WebSocket ?= null

    var okHttpClient : OkHttpClient ?= null




    //endregion


    //region Functions



      fun sendSubscribe(subscribe: SocketSubscribe) = safeExecute(SocketSubscribe::class.java,subscribe)

      fun sendImOnline(imOnline: ImOnline)  = safeExecute(ImOnline::class.java,imOnline)

      fun startCall(startCall : StartCall) = safeExecute(StartCall::class.java,startCall)

      fun sendSdpData(sdpMessage : String) = safeExecute(null,null,sdpMessage)

     fun sendTanResponse(tanResponse: TanResponse) = safeExecute(TanResponse::class.java,tanResponse)

     fun sendCameraChanged(toggle: CameraToggle) = safeExecute(CameraToggle::class.java,toggle)

    fun sendFlashChanged(toggle: FlashToggle) = safeExecute(FlashToggle::class.java,toggle)


    private fun start() {
        val request: Request = Request.Builder().url(SOCKET_BASE_URL).build()
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                socketConnectionStatusListener(SocketConnectionStatus.OPEN)
                socketResponseListener?.onSocketResponse(
                    Success(
                        SocketResponse(SocketConnectionStatus.OPEN.type)
                    )
                )

            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                if (code != 4001) {
                    socketConnectionStatusListener(SocketConnectionStatus.CLOSE)
                }
                disconnected()
                socketResponseListener?.onSocketResponse(
                    Failure(
                        SocketConnectionError()
                    )
                )
                println("SOKETTTTT KAPANDIIIII")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("comed actionn = $text")
                text.let {
                    moshi.adapter<SocketResponse>(SocketResponse::class.java).fromJson(it)
                        ?.let { socketResp ->
                            Log.d("actionnn = ",socketResp.action)
                            socketResponseListener?.onSocketResponse(
                                Success(
                                    socketResp
                                )
                            )
                        }
                }



            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                disconnected()
                socketConnectionStatusListener(SocketConnectionStatus.EXCEPTION)
                socketResponseListener?.onSocketResponse(
                    Failure(
                        SocketConnectionError()
                    )
                )
                println("SOKETTTTT HATALIIIII")
            }


        })

    }


    fun ifExistSocketConnection() {
        if (webSocket == null && socketResponseListener == null) {
            start()
        }
    }







        @ExperimentalCoroutinesApi
        suspend fun socketEvent() : Flow<Result<SocketResponse>>  = callbackFlow{
            socketResponseListener = object : SocketResponseListener{
                    override fun onSocketResponse(socketResponse: Result<SocketResponse>) {
                        socketResponse.onFailure {
                                this@callbackFlow.close()

                        }
                        socketResponse.onSuccess {
                            offer(socketResponse)
                        }

                    }

                }
                awaitClose {
                    socketResponseListener = null
                }
        }


         fun disconnected(){
            okHttpClient?.dispatcher?.cancelAll()
            okHttpClient = null
            webSocket = null
        }










    private  fun <T> safeExecute(
        clas : Class<T>? = null,
        instance: T? = null,
        readyData: String? = null) =
      // if (connectionLiveData?.value == true) {
            try {
                if (webSocket != null ){

                    readyData?.let { data ->
                        webSocket?.send(data)
                    }

                    clas?.let { cls ->
                        instance?.let { inst ->
                            val jsonAdapter = moshi.adapter(cls)
                            val json = jsonAdapter.toJson(inst)
                            Log.d("actionnn sended =",json.toString())
                            webSocket?.send(json)
                            Success(SocketResponse(action = SocketActionType.MESSAGE_SENDED.type))
                        }
                    }

                }else{
                    Failure(SocketConnectionError())
                }

            } catch (e: IOException) {
                Failure(TimeoutError())
            }
      /* } else {
           Failure(NetworkError())
        }*/





    //endregion

}