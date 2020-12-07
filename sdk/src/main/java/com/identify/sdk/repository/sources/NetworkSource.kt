/*
package com.identify.sdk.repository.sources

import android.net.NetworkInfo
import com.evren.repository.NetworkRepository
import com.evren.repository.interactors.BaseApiResponse
import com.evren.repository.interactors.base.*
import com.evren.repository.model.CustomerInformationEntity
import com.evren.repository.model.dto.TanDto
import com.evren.repository.model.entities.TanEntity
import com.evren.repository.network.ApiImpl
import com.identify.sdk.repository.NetworkRepository
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.dto.TanDto
import com.identify.sdk.repository.model.entities.TanEntity
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

private const val DEFAULT_IMAGE_SIZE = 480

*/
/**
 * NetworkSource for fetching results using api and wrapping them as contracted in [repository][NetworkRepository],
 * returning either [failure][Failure] with proper [reason][Reason] or [success][Success] with data
 *//*

internal class NetworkSource @Inject constructor(
    private val apiImpl: ApiImpl,
    private val networkInfoProvider: Provider<NetworkInfo>
) : NetworkRepository() {

    //region Properties

    private val isNetworkConnected: Boolean
        get() {
            val networkInfo = networkInfoProvider.get()
            return networkInfo != null && networkInfo.isConnected
        }
    //endregion

    //region Functions


    override suspend fun getCustomerInformation(id: String): Result<CustomerInformationEntity> =
        safeExecute({
            apiImpl.getCustomerInformation(id)
        }) {
         it.data!!
        }

    override suspend fun setSmsCode(tanDto: TanDto): Result<TanEntity?> =
        safeExecute({
            apiImpl.setSmsCode(tanDto)
        }){
            it.data
        }

    private inline fun <T : BaseApiResponse<*>, R> safeExecute(
        block: () -> Response<T>,
        transform: (T) -> R
    ) =
        if (isNetworkConnected) {
            try {
                block().extractResponseBody(transform)
            } catch (e: IOException) {
                Failure(TimeoutError())
            }
        } else {
            Failure(NetworkError())
        }

    private inline fun <T : BaseApiResponse<*>, R> Response<T>.extractResponseBody(transform: (T) -> R) =
        if (isSuccessful) {
            body()?.let {
                if (it.result != null && it.result) {
                    Success(transform(it), it.messages)
                } else Failure(ApiError(it.messages))
            } ?: Failure(EmptyResultError())
        } else {
            Failure(ResponseError())
        }

    //endregion
}
*/
