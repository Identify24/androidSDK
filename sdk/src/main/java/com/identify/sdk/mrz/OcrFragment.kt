package com.identify.sdk.mrz

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.form.WebViewFormFragment
import com.identify.sdk.mrz.mlkit.camera.CameraSource
import com.identify.sdk.mrz.mlkit.text.TextRecognitionProcessor
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.mrz.DocType
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import kotlinx.android.synthetic.main.fragment_mrz.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jmrtd.lds.icao.MRZInfo
import java.io.IOException

class OcrFragment : BaseFragment() ,
    TextRecognitionProcessor.ResultListener {

    var cameraSource : CameraSource?= null
    var docType : DocType = DocType.ID_CARD
    var isOcrSuccess : Boolean = false
    private var onFragmentTransactionListener: OnFragmentTransactionListener ?= null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCameraSource()
    }


    override fun getLayoutId(): Int  = R.layout.fragment_mrz

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(activity, graphicsOverlay)
            cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
        }
        cameraSource?.setMachineLearningFrameProcessor(TextRecognitionProcessor(docType, this))
    }

    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                cameraSourcePreview.start(cameraSource, graphicsOverlay)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        cameraSourcePreview.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            onFragmentTransactionListener = activity as OnFragmentTransactionListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentTransactionListener = null
    }

    companion object {

        @JvmStatic
        fun newInstance() =
                OcrFragment()
    }


    override fun onSuccess(mrzInfo: MRZInfo?) {
        mrzInfo?.let {
            if (!isOcrSuccess){
                isOcrSuccess = true
                onFragmentTransactionListener?.onOpenNfcFragment(it)
            }

        }
    }

    override fun onError(exp: Exception?) {

    }

}