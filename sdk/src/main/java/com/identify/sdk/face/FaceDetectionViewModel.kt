package com.identify.sdk.face

import androidx.lifecycle.viewModelScope
import com.identify.sdk.base.BaseViewModel
import com.identify.sdk.base.onFailure
import com.identify.sdk.base.onSuccess
import com.identify.sdk.repository.model.enums.FaceDetectionProcessType
import com.identify.sdk.repository.model.enums.ToogleType
import com.identify.sdk.repository.model.socket.ResultToogle
import com.identify.sdk.repository.model.socket.SocketResponse
import com.identify.sdk.repository.soket.SocketSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class FaceDetectionViewModel : BaseViewModel<SocketResponse>()  {

    var leftEyesClosed = false
    var rightEyesClosed = false

    var activeFaceDetectionProcessType = FaceDetectionProcessType.SMILING
    var faceDetectionFinishedProcessType : FaceDetectionProcessType ? = null


    var socketSource : SocketSource = SocketSource.getInstance()


    @ExperimentalCoroutinesApi
    fun sendSmiling(room : String){
        viewModelScope.launch {
            socketSource.sendToogleStatus(ResultToogle(action = ToogleType.IS_SMILING.type,result = true,room = room))
                ?.onSuccess {
                    handleSuccess(it)
                }?.onFailure {
                    handleFailure(it)
                }
        }

    }


    fun errorFoundRetryAllProcess(){
         leftEyesClosed = false
         rightEyesClosed = false
         activeFaceDetectionProcessType = FaceDetectionProcessType.SMILING
         faceDetectionFinishedProcessType = null
    }
}