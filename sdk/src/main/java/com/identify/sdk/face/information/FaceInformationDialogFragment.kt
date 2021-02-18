package com.identify.sdk.face.information

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.airbnb.lottie.LottieDrawable
import com.identify.sdk.R
import com.identify.sdk.base.BaseDialogFragment
import com.identify.sdk.face.FaceDetectionFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_face_information.*

class FaceInformationDialogFragment : BaseDialogFragment() {

    @DrawableRes
    private var resourceId : Int? = null

    private var infoContentText : String ?= null

    private var listener : FaceInformationStatusListener ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            infoContentText =  it.getString("infoContentText")
            resourceId = it.getInt("resourceId")
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_face_information


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        resourceId?.let { animDetectionStatus.setAnimation(it) }
        animDetectionStatus.repeatCount = LottieDrawable.INFINITE
        animDetectionStatus.playAnimation()
        tvInfoContent.text = infoContentText
        continueBtn.setOnClickListener {
            listener?.continueFaceDetection()
            dismiss()
        }


        closeBtn.setOnClickListener {
            listener?.continueFaceDetection()
            dismiss()
        }
        onBackPressClicked()
    }

    fun onBackPressClicked(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is FaceDetectionFragment) {
            listener = parentFragment as FaceInformationStatusListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }



    companion object {

        @JvmStatic
        fun newInstance(@DrawableRes resourceId : Int,infoContentText : String) =
            FaceInformationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("infoContentText",infoContentText)
                    putInt("resourceId",resourceId)
                }
            }
    }

    interface FaceInformationStatusListener{
        fun continueFaceDetection()
    }
}