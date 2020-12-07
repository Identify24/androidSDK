package com.identify.sdk.webrtc

interface OnFragmentTransactionListener {
    fun onOpenWaitFragment()
    fun onOpenCallingFragment()
    fun onOpenStartedCallFragment()
    fun onRemoveCallingFragment()
    fun onRemoveStartedCallFragment()
}