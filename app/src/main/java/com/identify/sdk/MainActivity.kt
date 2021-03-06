package com.identify.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.identify.sdk.repository.model.dto.MrzDto
import com.identify.sdk.repository.model.enums.IdentityType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val options = IdentityOptions.Builder()
            .setIdentityType(IdentityType.FULL_PROCESS)
            .setNfcExceptionCount(3)
            .setCallConnectionTimeOut(5000)
            .setOpenIntroPage(true)
            .build()

        val identifyObject = IdentifySdk.Builder()
            .api("https://api.kimlikbasit.com")
            .socket("wss://ws.kimlikbasit.com","8888")
            .stun("stun:stun.l.google.com","19302")
            .turn("turn:18.156.205.32","3478","test","test")
            .options(options)
            .build()


        identifyObject.startIdentification(this,"8f41593d-7699-11eb-8802-06a1762f812e","tr")

        identifyObject.identifyErrorListener = object : IdentifyErrorListener{
            override fun identError(errorMessage: String) {
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
            }
        }
        identifyObject.identifyResultListener = object : IdentifyResultListener{
            override fun nfcProcessFinished(isSuccess: Boolean, mrzDto: MrzDto?) {
                Toast.makeText(this@MainActivity,"nfc process finished",Toast.LENGTH_SHORT).show()
            }

            override fun vitalityDetectionProcessFinished() {
               Toast.makeText(this@MainActivity,"face process finished",Toast.LENGTH_SHORT).show()
            }
            override fun callProcessFinished() {
                Toast.makeText(this@MainActivity,"call process finished",Toast.LENGTH_SHORT).show()
                identifyObject.closeSdk() // You can finish sdk with this method when in the process you want
            }

        }

        identifyObject.sdkLifeCycleListener = object : SdkLifeCycleListener {
            override fun sdkDestroyed() {
                // Sdk Activity Destroyed
            }

            override fun sdkPaused() {
                // Sdk Activity Paused
            }

            override fun sdkResumed() {
                // Sdk Activity Resumed
            }

        }
    }




}