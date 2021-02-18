package com.identify.sdk

interface SdkLifeCycleListener {
    fun sdkDestroyed()
    fun sdkPaused()
    fun sdkResumed()
}