package com.identify.sdk.repository.network

import com.identify.sdk.SdkApp.BASE_URL
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.dto.MrzDto
import com.identify.sdk.repository.model.dto.TanDto
import com.identify.sdk.repository.model.entities.TanEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal const val TIMEOUT_DURATION = 7L

class ApiImpl  :
    Api {

    //region Properties

     val service by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                    .addInterceptor {chain->
                        val newBuilder = chain.request().newBuilder()
                        chain.proceed(newBuilder.build())
                    }
                    .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()
            .create(Api::class.java)
    }
    //endregion

    //region Functions


    override fun getCustomerInformation(
        id: String
    ): Call<BaseApiResponse<CustomerInformationEntity>> =
        service.getCustomerInformation(id)

    override fun setSmsCode(tanDto: TanDto): Call<BaseApiResponse<TanEntity?>> =
        service.setSmsCode(tanDto)

    override fun setMrzData(mrzDto: MrzDto): Call<BaseApiResponse<CustomerInformationEntity?>> =
        service.setMrzData(mrzDto)
    //endregion
}
