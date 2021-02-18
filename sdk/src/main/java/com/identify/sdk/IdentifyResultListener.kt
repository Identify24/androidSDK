package com.identify.sdk

import com.identify.sdk.repository.model.dto.MrzDto

interface IdentifyResultListener {
    fun nfcProcessFinished(isSuccess : Boolean, mrzDto: MrzDto?)
    fun vitalityDetectionProcessFinished()
    fun callProcessFinished()
}