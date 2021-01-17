package com.identify.sdk.repository.network

import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.dto.MrzDto
import com.identify.sdk.repository.model.dto.TanDto
import com.identify.sdk.repository.model.entities.TanEntity
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for networking
 */
interface Api {


    @Headers("Content-Type: application/json")
    @GET("mobile/getIdentDetails/{id}")
      fun getCustomerInformation(
        @Path("id") id: String
    ): Call<BaseApiResponse<CustomerInformationEntity>>

    @Headers("Content-Type: application/json")
    @POST("mobile/verifyTan")
     fun setSmsCode(
        @Body tanDto: TanDto
    ): Call<BaseApiResponse<TanEntity?>>

    @Headers("Content-Type: application/json")
    @POST("mobile/nfc_verify")
     fun setMrzData(
        @Body mrzDto: MrzDto
    ): Call<BaseApiResponse<CustomerInformationEntity?>>
}
