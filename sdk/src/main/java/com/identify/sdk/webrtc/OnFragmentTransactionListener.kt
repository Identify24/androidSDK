package com.identify.sdk.webrtc

import org.jmrtd.lds.icao.MRZInfo

interface OnFragmentTransactionListener {
    fun onOpenWaitFragment()
    fun onOpenCallingFragment()
    fun onOpenStartedCallFragment()
    fun onRemoveCallingFragment()
    fun onRemoveStartedCallFragment()
    fun onOpenNfcFragment(mrzInfo: MRZInfo)
    fun onRemoveNfcFragment()
    fun onOpenOcrFragment()
    fun onRemoveOcrFragment()
    fun onRemoveFaceDetectionFragment()
    fun onOpenFaceDetectionFragment()
    fun onOpenThankYouFragment()
    fun onRemoveThankYouFragment()
    fun onRemoveWaitFragment()
}