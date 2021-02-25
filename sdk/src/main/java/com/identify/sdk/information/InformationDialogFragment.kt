package com.identify.sdk.information

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import com.airbnb.lottie.LottieDrawable
import com.identify.sdk.R
import com.identify.sdk.base.BaseDialogFragment
import com.identify.sdk.face.FaceDetectionFragment
import kotlinx.android.synthetic.main.dialog_information.*

class InformationDialogFragment : BaseDialogFragment() {

    @DrawableRes
    private var animResourceId : Int? = null

    @DrawableRes
    private var imgResourceId : Int? = null

    private var infoContentText : String ?= null

    private var infoTitleText : String ?= null

    private var listener : FaceInformationStatusListener?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            infoContentText =  it.getString("infoContentText")
            infoTitleText =   it.getString("infoTitleText")
            animResourceId = it.getInt("animResourceId")
            imgResourceId = it.getInt("imgResourceId")
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_information


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        animResourceId?.let { animDetectionStatus.setAnimation(it) }
        imgResourceId?.let {  }
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
        fun newInstance(@DrawableRes animResourceId : Int?,@DrawableRes imgResourceId : Int?,infoTitleText : String,infoContentText : String) =
            InformationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("infoContentText",infoContentText)
                    putString("infoTitleText",infoTitleText)
                    animResourceId?.let { putInt("animResourceId", it) }
                    imgResourceId?.let { putInt("imgResourceId",it) }
                }
            }
    }

    interface FaceInformationStatusListener{
        fun continueFaceDetection()
    }
}