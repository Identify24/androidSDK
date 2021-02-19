package com.identify.sdk.webrtc

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.identify.sdk.base.*
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.dto.TanDto
import com.identify.sdk.repository.model.entities.TanEntity
import com.identify.sdk.repository.model.enums.NfcStatusType
import com.identify.sdk.repository.model.enums.SdpType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
import com.identify.sdk.repository.model.enums.ToogleType
import com.identify.sdk.repository.model.socket.*
import com.identify.sdk.repository.network.Api
import com.identify.sdk.repository.network.ApiImpl
import com.identify.sdk.repository.soket.RtcConnectionSource
import com.identify.sdk.repository.soket.SocketSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@ExperimentalCoroutinesApi
class CallViewModel  : BaseViewModel<SocketResponse>() {


     val apiImpl : ApiImpl by lazy {
         ApiImpl()
     }

     var socketSource : SocketSource  = SocketSource.getInstance()

     var rtcConnectionSource : RtcConnectionSource  = RtcConnectionSource.getInstance()

     var customerInformationEntity : CustomerInformationEntity?= null

    val socketStatus = MutableLiveData<SocketConnectionStatus>()

    val activitySocketResponse = MutableLiveData<SocketResponse>()

    var nfcStatusType : NfcStatusType ?= null
    var activityFailure = MutableLiveData<Boolean>()

    var callStarted  = false
    var goThankYouFragment = false

    private var currentCamera = CAMERA_FRONT



    val tanResponse = MutableLiveData<TanEntity>()
    var tanId : String ?= null
    var tanCode : String ?= null


    fun initRtcResource(context: Context){
        customerInformationEntity?.let { rtcConnectionSource.init(it,context,socketSource) }
    }


    fun observeSocketStatus(){
        viewModelScope.launch {
            socketSource.socketConnectionStatusListener = {
                socketStatus.postValue(it)
            }
        }

    }


     fun connectSocket(isComingNfc : Boolean,nfcStatusType : NfcStatusType ) {
         viewModelScope.launch {
             socketSource.ifExistSocketConnection()
             socketSource.socketEvent().collect {
                 when (it) {
                     is Success ->{
                         when(it.successData.action){
                             SocketConnectionStatus.OPEN.type -> {
                                 when(isComingNfc){
                                     true->{
                                         val resp = sendNfcSubscribe(isComingNfc)
                                         when(resp){
                                             is Success->{
                                                 if ( nfcStatusType == NfcStatusType.NOT_AVAILABLE) {
                                                     sendNfcStatus(nfcStatusType)
                                                         .onSuccess { response -> 
                                                             handleActivitySuccess(response)
                                                         }
                                                         .onFailure { handleActivityFailure() }
                                                 }
                                                 else {
                                                     resp.onSuccess { response->
                                                        handleActivitySuccess(response)
                                                     }
                                                     resp.onFailure {
                                                         handleActivityFailure()
                                                     }
                                                 }
                                             }
                                             is Failure->{
                                                 handleActivityFailure()
                                             }
                                         }

                                     }
                                     false->{
                                         val resp = sendNfcSubscribe(isComingNfc)
                                         resp.onSuccess {response -> 
                                             handleActivitySuccess(response) }
                                         resp.onFailure {handleActivityFailure() }
                                     }
                                 }
                             }
                             SocketActionType.NEW_SUBSCRIBE.type->{
                                 val resp = sendImOnline()
                                 resp.onSuccess {
                                     handleSuccess(it)
                                 }
                                 resp.onFailure {
                                     handleFailure(it)
                                 }
                             }
                             SocketActionType.SDP.type ->{
                                 val res = rtcConnectionSource.handleSdpMessage(socketResponse = it.successData)
                                 handleSuccess(SocketResponse(action = res))
                             }
                             SocketActionType.CANDIDATE.type ->{
                                 val res = rtcConnectionSource.handleSdpMessage(socketResponse = it.successData)
                                 handleSuccess(SocketResponse(action = res))
                             }
                             else->{
                                 handleSuccess(it.successData)
                             }
                         }
                     }
                     is Failure ->{
                         handleFailure(it.errorData)
                     }
                 }
             }
         }
     }

    fun handleActivityFailure(){
        activityFailure.value = true
    }
    fun handleActivitySuccess(socketResponse: SocketResponse){
        activitySocketResponse.value = socketResponse
    }
    
    fun disconnectSocket(){
        closeSocketStream()
    }

    fun closeSocketStream(){
        socketSource.webSocket?.let {
            it.close(4001,"")
        }

    }




    suspend fun startRtcProcess() : String?{
        return rtcConnectionSource.handleSdpMessage(SocketResponse(SocketActionType.SDP.type,sdp = Sdp(
            type = SdpType.READY.type,sdp = null)
        ))
    }



    fun sendNewSubscribe() : Result<SocketResponse> {
        customerInformationEntity?.customerUid?.let { id ->
            return socketSource.sendSubscribe(SocketSubscribe(room = id))!!
        }
        return Failure(AuthenticationError())
    }

    fun sendNfcStatus(nfcStatusType: NfcStatusType) : Result<SocketResponse> {
        customerInformationEntity?.customerUid?.let { id ->
            return socketSource.sendNfcStatusType(NfcStatus(status = nfcStatusType.type,room = id))!!
        }
        return Failure(AuthenticationError())
    }

    fun sendNfcSubscribe(isComingNfc: Boolean) : Result<SocketResponse> {
        customerInformationEntity?.customerUid?.let { id ->
            return socketSource.sendSubscribe(if (isComingNfc)SocketSubscribe(location = "NFCCheck",room = id) else SocketSubscribe(room = id))!!
        }
        return Failure(AuthenticationError())
    }

    fun sendImOnline(): Result<SocketResponse> {
        customerInformationEntity?.customerUid?.let { id ->
            return socketSource.sendImOnline(ImOnline(room = id))!!
        }
        return Failure(AuthenticationError())
    }


    fun finishCall(){
        customerInformationEntity?.customerUid?.let { room->
            viewModelScope.launch {
                socketSource.sendFinishCall(CallRejected(room = room))
                    ?.handle(::handleState, ::handleFailure, ::handleSuccess)
            }
        }
    }




    fun startRtcProccessing(){
        viewModelScope.launch {
           customerInformationEntity?.customerUid?.let {
                sendStartCall(it)
                    .handle(::handleState, ::handleFailure, ::handleSuccess)
            }
        }

    }

    fun toogleIdGuideStatusChanged(result: Boolean) {
        customerInformationEntity?.customerUid?.let { room ->
            viewModelScope.launch {
                socketSource.sendToogleStatus(
                        ResultToogle(
                                action = ToogleType.TOOGLE_ID_GUIDE.type,
                                result = result,
                                room = room
                        )
                )?.onFailure {
                    handleFailure(it)
                }
            }
        }
    }

    fun switchCamera(){
        rtcConnectionSource.switchCamera()
        currentCamera = if (currentCamera == CAMERA_FRONT) CAMERA_BACK else CAMERA_FRONT
    }

    fun toogleFaceGuideStatusChanged(result: Boolean){
        customerInformationEntity?.customerUid?.let { room ->
            viewModelScope.launch {
                socketSource.sendToogleStatus(
                        ResultToogle(
                                action = ToogleType.TOOGLE_FACE_GUIDE.type,
                                result = result,
                                room = room
                        )
                )?.onFailure {
                    handleFailure(it)
                }
            }
        }

    }

    fun closeStream() {
        rtcConnectionSource.dispose()
    }

    fun enableCamera(enabled: Boolean) {
        rtcConnectionSource.cameraEnabled = enabled
    }

    fun enableMicrophone(enabled: Boolean) {
        rtcConnectionSource.microphoneEnabled = enabled
    }

    suspend fun sendStartCall(id : String) : Result<SocketResponse> {

        return when(val res = socketSource.startCall(StartCall(room = id))!!){
            is Success ->{
                startRtcProcess()
                res
            }
            else -> {
                res
            }
        }
    }


    fun setSmsCode(){
        tanId?.let { tid ->
            tanCode?.let { code ->
                viewModelScope.launch {
                    handleState(State.Loading())

                    apiImpl.service?.setSmsCode(TanDto(tid,code))?.enqueue( object :
                        Callback<BaseApiResponse<TanEntity?>> {
                        override fun onFailure(
                            call: Call<BaseApiResponse<TanEntity?>>,
                            t: Throwable
                        ) {
                            t.message?.let { handleFailure(ResponseError()) }
                            handleState(State.Loaded())
                        }

                        override fun onResponse(
                            call: Call<BaseApiResponse<TanEntity?>>,
                            response: Response<BaseApiResponse<TanEntity?>>
                        ) {
                            if (response.isSuccessful && response.body()?.result == true){
                                response.body()?.data.let {
                                    tanResponse.value = it
                                    tanCodeResultWillSendWithSocket()
                                }
                            }else{
                                if (response.body()?.messages?.get(0).isNullOrEmpty()) handleFailure(ResponseError()) else handleFailure(ApiError(response.body()?.messages))

                            }
                            handleState(State.Loaded())
                        }

                    })
                }
            }
        }
    }

    private fun tanCodeResultWillSendWithSocket(){
        customerInformationEntity?.customerUid?.let { room ->
            tanId?.let { tanId ->
                tanCode?.let {code ->
                    viewModelScope.launch {
                        socketSource.sendTanResponse(TanResponse(room = room,tid = tanId,tan = code))?.onFailure {
                            handleFailure(it)
                        }
                    }
                }
            }
        }
    }



    companion object{
        val CAMERA_FRONT = "1"
        val CAMERA_BACK = "0"
    }

}