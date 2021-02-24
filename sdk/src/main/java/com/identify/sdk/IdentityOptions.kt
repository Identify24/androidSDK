package com.identify.sdk

import com.identify.sdk.repository.model.enums.IdentityType

class IdentityOptions {

     private var identityType: IdentityType = IdentityType.FULL_PROCESS

     private var nfcExceptionCount: Int = 3

    private var openIntroPage  = true

    private var callConnectionTimeOut : Long  = 10000

     open class Builder {

         private val identityOptions  by lazy {
             IdentityOptions()
         }




        fun setIdentityType(identityType: IdentityType) = apply { identityOptions.identityType = identityType }
        fun setNfcExceptionCount(nfcExceptionCount : Int) = apply { identityOptions.nfcExceptionCount = nfcExceptionCount }
        fun setOpenIntroPage(openIntroPage : Boolean) = apply { identityOptions.openIntroPage = openIntroPage }
        fun setCallConnectionTimeOut(callConnectionTimeOut : Long) = apply { identityOptions.callConnectionTimeOut = callConnectionTimeOut }
        fun build() = identityOptions

    }

    fun getIdentityType() = identityType

    fun getNfcExceptionCount() = nfcExceptionCount

    fun getOpenIntroPage() = openIntroPage

    fun getCallConnectionTimeOut() = callConnectionTimeOut

}
