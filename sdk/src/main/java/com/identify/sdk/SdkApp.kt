package com.identify.sdk

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.identify.sdk.repository.model.enums.IdentityResultType
import com.identify.sdk.repository.model.enums.IdentityType

object SdkApp {


    var SOCKET_BASE_URL : String ?= null
    var BASE_URL : String ?= null
    var STUN_URL : String ?= null
    var TURN_URL : String ?= null
    var USERNAME : String ?= null
    var PASSWORD : String ?= null
    var identityOptions : IdentityOptions ?= null


    init {
        identityOptions = IdentityOptions.Builder().setIdentityType(IdentityType.FULL_PROCESS).setNfcExceptionCount(3).setOpenIntroPage(true).setCallConnectionTimeOut(10000).build()
    }



    fun destroy(){
         SOCKET_BASE_URL = null
         BASE_URL = null
         STUN_URL = null
         TURN_URL = null
         USERNAME = null
         PASSWORD = null
         identityOptions = null
    }

}