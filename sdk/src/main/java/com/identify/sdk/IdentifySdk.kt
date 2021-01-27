package com.identify.sdk

import android.content.Context
import android.content.Intent
import com.identify.sdk.base.*
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.network.ApiImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException

class IdentifySdk {





    open class Builder {

        private var baseApiUrl: String ?= null
        private var baseSocketUrl: String ?= null
        private var socketPort: String ?= null
        private var stunUrl: String ?= null
        private var stunPort: String ?= null
        private var turnUrl: String ?= null
        private var turnPort: String ?= null
        private var username: String ?= null
        private var password: String ?= null

        fun api(baseApiUrl: String) = apply { this.baseApiUrl = baseApiUrl }
        fun socket(baseSocketUrl: String,socketPort: String) = apply { this.baseSocketUrl = baseSocketUrl; this.socketPort = socketPort }
        fun stun(stunUrl: String, stunPort: String) = apply { this.stunUrl = stunUrl;this.stunPort = stunPort }
        fun turn(turnUrl: String, turnPort: String,username: String, password: String) = apply { this.turnUrl = turnUrl;this.turnPort = turnPort;this.username = username;this.password = password }
        fun build() = if (baseApiUrl == null
                || baseSocketUrl == null || socketPort == null
                || stunUrl == null || stunPort == null
                || turnUrl == null || turnPort == null
                || username == null || password == null) throw NullPointerException("Builder variants can not be null")
        else {
            setEnvironmentVariables()
            getInstance()
        }

        private fun setEnvironmentVariables(){
            BASE_URL = baseApiUrl.toString()+"/"
            SOCKET_BASE_URL = baseSocketUrl.toString()+":"+socketPort.toString()+"/"
            STUN_URL = stunUrl.toString()+":"+stunPort.toString()
            TURN_URL = turnUrl.toString()+":"+turnPort.toString()
            USERNAME = username.toString()
            PASSWORD = password.toString()

        }
    }

    private val apiImp : ApiImpl by lazy {
        ApiImpl()
    }

    var identifyErrorListener : IdentifyErrorListener ?= null



    companion object{
        private val identifySdk by lazy {
            IdentifySdk()
        }
        @JvmStatic
        private fun getInstance() : IdentifySdk =
            identifySdk
    }






    private fun verifyIdentificationId(identificationID: String, successListener : (customerInformation : CustomerInformationEntity?) -> Unit, identificationErrorListener: (errorText : String)-> Unit){
       apiImp.service.getCustomerInformation(identificationID).enqueue(object :Callback<BaseApiResponse<CustomerInformationEntity>>{
                override fun onFailure(
                    call: Call<BaseApiResponse<CustomerInformationEntity>>,
                    t: Throwable
                ) {
                    t.message?.let { identificationErrorListener(it) }
                }

                override fun onResponse(
                    call: Call<BaseApiResponse<CustomerInformationEntity>>,
                    response: Response<BaseApiResponse<CustomerInformationEntity>>
                ) {
                    if (response.isSuccessful && response.body()?.result == true){
                        response.body()?.data.let {
                            successListener(it)
                        }
                    }else{
                        if (response.body()?.messages?.get(0).isNullOrEmpty()) identificationErrorListener(response.message()) else response.body()?.messages?.get(0)?.let {
                            identificationErrorListener(it)
                        }

                    }
                }

            })

    }



     fun startIdentification(context: Context, identificationID: String,language : String?=null){
        verifyIdentificationId(identificationID,{ customer ->
            val intent = Intent(context, IdentifyActivity::class.java)
            intent.putExtra("customer",customer)
            intent.putExtra("language",language)
            context.startActivity(intent)
        },{
            identifyErrorListener?.identError(it)
        })
        }




}