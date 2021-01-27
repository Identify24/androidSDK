package com.identify.sdk.repository.soket

import android.content.Context
import android.media.AudioManager
import com.identify.sdk.base.PASSWORD
import com.identify.sdk.base.STUN_URL
import com.identify.sdk.base.TURN_URL
import com.identify.sdk.base.USERNAME
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SdpType
import com.identify.sdk.repository.model.socket.CameraToggle
import com.identify.sdk.repository.model.socket.DataChannelMessage
import com.identify.sdk.repository.model.socket.SocketResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@ExperimentalCoroutinesApi
class RtcConnectionSource {


    companion object{
        private val rtcConnectionSource by lazy {
            RtcConnectionSource()
        }
        fun getInstance() : RtcConnectionSource =
            rtcConnectionSource
    }

    fun init(customerInformationEntity: CustomerInformationEntity, context: Context, socketSource: SocketSource){
        this.customerInformationEntity = customerInformationEntity
        this.context = context
        this.socketSource = socketSource

    }


    private var customerInformationEntity : CustomerInformationEntity ?= null
    private var context: Context ?= null
    private var socketSource : SocketSource ?= null

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    private var videoCapturer: CameraVideoCapturer? = null


    private var audioTrack : AudioTrack?= null


    private val audioManager: AudioManager by lazy { context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager }


    private var isInitiator: Boolean = false
    private var isChannelReady: Boolean = false

    var rootEglBase: EglBase? = null
    var surfaceViewRendererLocal: SurfaceViewRenderer? = null
    var surfaceViewRendererRemote: SurfaceViewRenderer? = null
    var dataChannelMessageObserver: ((DataChannelMessage) -> Unit)? = null

    private val sdpConstraints: MediaConstraints by lazy {
        MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
    }


    var cameraEnabled = true
        set(isEnabled) {
            field = isEnabled
            videoCapturer?.let {
                enableVideo(isEnabled, it)
            }
        }
    var microphoneEnabled = true
        set(isEnabled) {
            field = isEnabled
                audioTrack?.let {
                    it.setEnabled(isEnabled)
                }
        }

    fun switchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler? = null){
        try {
            videoCapturer?.switchCamera(cameraSwitchHandler)
            socketSource?.sendCameraChanged(CameraToggle(result = true))
        }catch (e: Exception){
            socketSource?.sendCameraChanged(CameraToggle(result = false))
        }
    }






     fun enableVideo(isEnabled: Boolean, videoCapturer: VideoCapturer) {
        if (isEnabled) {
            videoCapturer.startCapture(1280, 720, 24)
        } else {
            videoCapturer.stopCapture()
        }
    }

    fun dispose() {
        peerConnection?.dispose()
        peerConnection = null
        videoCapturer?.stopCapture()
        videoCapturer = null
        videoCapturer?.dispose()
        videoCapturer = null
        dataChannel?.dispose()
        dataChannel = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        isChannelReady = false
        isInitiator = false
        videoCapturer = null
        dataChannel = null
        peerConnection = null
      //  signallingServerDataSource.emitEvent(SignallingServerDataSource.EVENT_BYE, null)
    }


    fun emitOnDataChannel(json: JSONObject) = emitOnDataChannel(json.toString())

    fun emitOnDataChannel(string: String) {
        val byteBuffer = ByteBuffer.wrap(string.toByteArray())
        val dataChannelBuffer = DataChannel.Buffer(byteBuffer, false)
        dataChannel?.send(dataChannelBuffer)
    }


    private fun createPeerConnection() {
        initializePeerConnectionFactory()

        val options = PeerConnectionFactory.Options()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()

        peerConnectionFactory?.setVideoHwAccelerationOptions(rootEglBase?.eglBaseContext, rootEglBase?.eglBaseContext)

        val localMediaStream = peerConnectionFactory?.createLocalMediaStream(UUID.randomUUID().toString())



        // Local Audio Stream
        val audioSource = peerConnectionFactory?.createAudioSource(sdpConstraints)
        audioTrack = peerConnectionFactory?.createAudioTrack(UUID.randomUUID().toString(), audioSource)
        localMediaStream?.addTrack(audioTrack)

        // Local Video Stream
        videoCapturer = createCameraCapturer(Camera2Enumerator(context))

        val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer as VideoCapturer)
        val videoTrack = peerConnectionFactory?.createVideoTrack(UUID.randomUUID().toString(), videoSource)

        videoTrack?.setEnabled(true)
        videoCapturer?.startCapture(1280, 720, 24)
        localMediaStream?.addTrack(videoTrack)

        // Attach to view
        videoTrack?.addSink(surfaceViewRendererLocal)

        // Create Connection
        val stunServer = PeerConnection.IceServer.builder(STUN_URL).createIceServer()
        val turnServer = PeerConnection.IceServer.builder(TURN_URL).setUsername(USERNAME).setPassword(PASSWORD).createIceServer()
        peerConnection = peerConnectionFactory?.createPeerConnection(listOf(stunServer,turnServer), peerConnectionObserver)

        if (isInitiator) {
            val init = DataChannel.Init()
            dataChannel = peerConnection?.createDataChannel(UUID.randomUUID().toString(), init)
            dataChannel?.registerObserver(dataChannelObserver(dataChannel))
        }

        peerConnection?.addStream(localMediaStream)


    }

    private fun initializePeerConnectionFactory() =
        PeerConnectionFactory.InitializationOptions.builder(context)
        .setEnableVideoHwAcceleration(true)
        .setEnableInternalTracer(false)
        .createInitializationOptions().let {
            PeerConnectionFactory.initialize(it)
        }



     suspend fun handleSdpMessage(socketResponse : SocketResponse) = suspendCoroutine<String> { cont ->
            when (socketResponse.action) {
                SocketActionType.SDP.type->{
                    when(socketResponse.sdp?.type){
                        SdpType.READY.type -> {
                            createPeerConnection()
                            isChannelReady = true
                            createOffer()
                            cont.resume(SdpType.READY.type)
                        }
                        SdpType.OFFER.type -> {
                            SessionDescription(SessionDescription.Type.OFFER, socketResponse.sdp.sdp).let {
                                onOffer(it)
                                cont.resume(SdpType.OFFER.type)
                            }
                        }
                        SdpType.ANSWER.type -> {
                            SessionDescription(SessionDescription.Type.ANSWER, socketResponse.sdp.sdp).let {
                                onAnswer(it)
                                cont.resume(SdpType.ANSWER.type)
                            }
                        }
                    }
                }
                SocketActionType.CANDIDATE.type->{
                    socketResponse.candidate?.sdpMLineIndex?.let { index->
                        val candidate = IceCandidate(
                            socketResponse.candidate.sdpMid,
                            index,
                            socketResponse.candidate.candidate
                        )
                        peerConnection?.addIceCandidate(candidate)
                    }
                    cont.resume(SdpType.CANDIDATE.type)
                }
            }


    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): CameraVideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private val peerConnectionObserver = object : PeerConnection.Observer {

        override fun onIceCandidate(candidate: IceCandidate) {
           customerInformationEntity?.customerUid?.let { id ->
                val json = JSONObject().apply {
                    put("action", "candidate")
                    put("candidate", JSONObject().put("candidate",candidate.sdp).put("sdpMid",candidate.sdpMid).put("sdpMLineIndex",candidate.sdpMLineIndex))
                    put("room", id)
                }
                socketSource?.sendSdpData(json.toString())
            }

        }

        override fun onDataChannel(dc: DataChannel?) {
            if (!isInitiator) {
                dataChannel = dc
                dataChannel?.registerObserver(dataChannelObserver(dataChannel))
            }
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {

        }

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            if (state == PeerConnection.IceConnectionState.DISCONNECTED) {
                audioManager.mode = AudioManager.MODE_NORMAL
            }
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {

        }

        override fun onAddStream(mediaStream: MediaStream?) {
            mediaStream?.videoTracks?.first()?.apply {
                setEnabled(true)
                addSink(surfaceViewRendererRemote)
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        }

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        }

        override fun onRemoveStream(p0: MediaStream?) {
        }

        override fun onRenegotiationNeeded() {
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        }
    }

    private fun createOffer() {
        peerConnection?.createOffer(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
                setLocalSdp(sdp)
            }
        }, sdpConstraints)
    }

    private fun createAnswer() {
        peerConnection?.createAnswer(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
                setLocalSdp(sdp)
            }
        }, sdpConstraints)
    }

    private fun onOffer(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetSuccess() {
                createAnswer()
            }
        }, sdp)
    }

    private fun onAnswer(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetSuccess() {

            }
        }, sdp)
    }

    private fun setLocalSdp(sdp: SessionDescription) {
        peerConnection?.setLocalDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
                customerInformationEntity?.customerUid?.let { id->
                    val json = JSONObject().apply {
                        put("action","sdp")
                        put("sdp", JSONObject().put("type",sdp.type.canonicalForm().toLowerCase()).put("sdp",sdp.description))
                        put("room", id)
                    }
                    socketSource?.sendSdpData(json.toString())
                }
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
            }

        }, sdp)
    }

    private fun dataChannelObserver(dc: DataChannel?): DataChannel.Observer = object : DataChannel.Observer {

        override fun onBufferedAmountChange(previousAmount: Long) {
        }

        override fun onStateChange() {
        }

        override fun onMessage(buffer: DataChannel.Buffer) {
            if (buffer.binary) {
                return
            }
            val data = buffer.data
            val bytes = ByteArray(data.capacity())
            data.get(bytes)
            val strData = String(bytes, Charset.forName("UTF-8"))
            try {
                val json = JSONObject(strData)
                val jsonData = json.getJSONObject("data")
                when (json.getString("type")) {
                    DataChannelMessage.TYPE_LOCATION -> onLocation(jsonData)
                    DataChannelMessage.TYPE_CHAT_MESSAGE -> onChatMessage(jsonData)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

        }

        private fun onChatMessage(data: JSONObject) {
            dataChannelMessageObserver?.invoke(DataChannelMessage(DataChannelMessage.TYPE_CHAT_MESSAGE, data))
        }

        private fun onLocation(data: JSONObject) {
            dataChannelMessageObserver?.invoke(DataChannelMessage(DataChannelMessage.TYPE_LOCATION, data))
        }
    }
}