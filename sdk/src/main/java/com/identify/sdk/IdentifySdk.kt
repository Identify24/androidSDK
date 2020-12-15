package com.identify.sdk

import android.content.Context
import android.content.Intent
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.network.ApiImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IdentifySdk {

    private val apiImp : ApiImpl by lazy {
        ApiImpl()
    }

    val identifyErrorListener : IdentifyErrorListener ?= null



    companion object{
        private val identifySdk by lazy {
            IdentifySdk()
        }
        @JvmStatic
        fun getInstance() : IdentifySdk =
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


     fun startIdentification(context: Context, identificationID: String){
        verifyIdentificationId(identificationID,{ customer ->
            val intent = Intent(context, IdentifyActivity::class.java)
            intent.putExtra("customer",customer)
            context.startActivity(intent)
        },{
            identifyErrorListener?.identError(it)
        })
        }




}