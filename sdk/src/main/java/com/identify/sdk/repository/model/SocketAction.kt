package com.identify.sdk.repository.model


enum class SocketActionType(val type : String) {
    SYSTEM_MESSAGE("sysMsg"),
    IM_ONLINE("imOnline"),
    IM_OFFLINE("imOffline"),
    NEW_SUBSCRIBE("newSub"),
    INIT_CALL("initCall"),
    START_CALL("startCall"),
    SDP("sdp"),
    CANDIDATE("candidate"),
    TERMINATE_CALL("terminateCall"),
    MESSAGE_SENDED("messageSended"),
    END_CALL("endCall"),
    REQUEST_TAN("requestTan"),
    TOGGLECAMERA("toggleCamera"),
    SUBREJECTED("subRejected"),
    FACE_GUIDE_ON("faceGuideOn"),
    FACE_GUIDE_OFF("faceGuideOff"),
    ID_GUIDE_ON("idGuideOn"),
    ID_GUIDE_OFF("idGuideOff"),
    SKIPNFC("skipNFC"),
    TOGGLEFlASH("toggleFlash");


    companion object {
        private val map = SocketActionType.values().associateBy(SocketActionType::type)
        fun fromString(type: String?) = map[type]
    }
}