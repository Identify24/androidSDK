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
        val identifyObject = IdentifySdk.Builder()
            .api("https://api.kimlikbasit.com")
            .socket("wss://ws.kimlikbasit.com","8888")
            .stun("stun:stun.l.google.com","19302")
            .turn("turn:18.156.205.32","3478","test","test")
            .options(IdentityOptions.Builder().setIdentityType(IdentityType.ONLY_CALL).build())
            .build()


        identifyObject.startIdentification(this,"cd57398a-5bfb-11eb-994a-06a1762f812e","tr")

        identifyObject.identifyErrorListener = object : IdentifyErrorListener{
            override fun identError(errorMessage: String) {
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
            }
        }
        identifyObject.identifyResultListener = object : IdentifyResultListener{
            override fun nfcProcessFinished(isSuccess: Boolean, mrzDto: MrzDto?) {
                Toast.makeText(this@MainActivity,mrzDto?.name+" için nfc bitti",Toast.LENGTH_SHORT).show()
            }

            override fun vitalityDetectionProcessFinished() {
               Toast.makeText(this@MainActivity,"face bitti",Toast.LENGTH_SHORT).show()
               // identifyObject.closeSdk()
            }
            override fun callProcessFinished() {
                Toast.makeText(this@MainActivity,"call bitti",Toast.LENGTH_SHORT).show()
                identifyObject.closeSdk()
            }

        }

        identifyObject.sdkLifeCycleListener = object : SdkLifeCycleListener {
            override fun sdkDestroyed() {

            }

            override fun sdkPaused() {

            }

            override fun sdkResumed() {

            }

        }
    }




}