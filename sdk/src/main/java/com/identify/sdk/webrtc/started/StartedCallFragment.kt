package com.identify.sdk.webrtc.started

import android.animation.Animator
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.SdkApp.identityOptions
import com.identify.sdk.base.ApiError
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.base.State
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SdpType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
import com.identify.sdk.util.changed
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.sure.AreYouSureDialogFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_bottom_tan.*
import kotlinx.android.synthetic.main.fragment_started_call.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.EglBase
import org.webrtc.RendererCommon


@ExperimentalCoroutinesApi
class StartedCallFragment : BaseFragment()    {


    //region Properties

    private val viewModel: CallViewModel by activityViewModels()

    private var rootEglBase: EglBase ?= null

    var mBottomSheetDialog : BottomSheetDialog ? = null

    var handler : Handler ?= null

    var runnable : Runnable ?= null

    var mBehavior : BottomSheetBehavior<View> ? = null

    private var onFragmentTransactionListener : OnFragmentTransactionListener ?= null

    var tanView: View ?= null



    //endregion





    //region Functions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataChanges()
        initSurfaces()
        viewModel.rtcConnectionSource.apply {
            rootEglBase = this@StartedCallFragment.rootEglBase
            this.surfaceViewRendererLocal = this@StartedCallFragment.surfaceViewRendererLocal
            this.surfaceViewRendererRemote = this@StartedCallFragment.surfaceViewRendererRemote
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            surfaceViewRendererRemote.visibility = View.VISIBLE
        }



        cameraEnabledToggle.setOnCheckedChangeListener { _, enabled ->
            viewModel.enableCamera(!enabled)
        }

        microphoneEnabledToggle.setOnCheckedChangeListener { _, enabled ->
            viewModel.enableMicrophone(!enabled)
        }

        disconnectButton.setOnClickListener {
           //goCallWaitFragmentFromFail()
        }


        onBackPressClicked()

        checkConnectionTimeOut()


    }

    private fun checkConnectionTimeOut() {
        handler = Handler()
        runnable = Runnable {
            if (!viewModel.callStarted){
                viewModel.finishCall()
                Toasty.error(requireContext(),getString(R.string.connection_error_when_calling),Toasty.LENGTH_LONG).show()
                goCallWaitFragmentFromFail()
            }
        }
        identityOptions?.getCallConnectionTimeOut()?.let { handler?.postDelayed(runnable, it) }
    }


    fun onBackPressClicked(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                Toasty.info(requireContext(),R.string.identify_is_in_progress,Toast.LENGTH_SHORT).show()
                return@OnKeyListener true
            }
            false
        })
    }


    private fun initSurfaces() {
        viewModel.initRtcResource(requireContext())
        rootEglBase = EglBase.create()
        surfaceViewRendererLocal.init(rootEglBase?.eglBaseContext, null)
        surfaceViewRendererLocal.setEnableHardwareScaler(true)
        surfaceViewRendererLocal.setMirror(false)
        surfaceViewRendererLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        surfaceViewRendererRemote.init(rootEglBase?.eglBaseContext, null)
        surfaceViewRendererRemote.setEnableHardwareScaler(true)
        surfaceViewRendererRemote.setMirror(false)
        surfaceViewRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
    }
/*
    override fun onStop() {
        super.onStop()

    }*/


    private fun goThankYouFragment(){
        viewModel.goThankYouFragment = true
        onFragmentTransactionListener?.onOpenThankYouFragment()
        onFragmentTransactionListener?.onRemoveStartedCallFragment()
    }

    private fun goCallWaitFragmentFromFail(){
        onFragmentTransactionListener?.onOpenWaitFragment()
        onFragmentTransactionListener?.onRemoveStartedCallFragment()
    }



     private fun observeDataChanges() {
        observe(viewModel.successData){
            when(it.action){
                SocketActionType.TERMINATE_CALL.type->{
                  if (viewModel.callStarted) goThankYouFragment()
                }
                SocketActionType.SDP.type->{

                }
                SdpType.ANSWER.type->{
                    viewModel.callStarted = true
                    relLayCallWaiting.visibility = View.GONE
                    surfaceViewRendererRemote.visibility = View.VISIBLE
                    surfaceViewRendererLocal.visibility = View.VISIBLE
                }
                SocketActionType.CANDIDATE.type->{

                }
                SocketActionType.REQUEST_TAN.type->{
                    viewModel.tanId = it.tId.toString()
                    showBottomSheetDialog()
                }
                SocketActionType.TOGGLECAMERA.type->{
                    viewModel.switchCamera()
                }
                SocketActionType.TOGGLEFlASH.type->{
                //    switchFlash()
                }
                SocketActionType.ID_GUIDE_ON.type->{
                    imgIdCard.visibility = View.VISIBLE
                    viewModel.toogleIdGuideStatusChanged(true)
                }
                SocketActionType.ID_GUIDE_OFF.type->{
                    imgIdCard.visibility = View.GONE
                    idLoadingAnimation.visibility =   View.VISIBLE
                    idLoadingAnimation.setAnimation(R.raw.nfc_success)
                    idLoadingAnimation.repeatCount = 0
                    idLoadingAnimation.playAnimation()
                    idLoadingAnimation.addAnimatorListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {
                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            viewModel.toogleIdGuideStatusChanged(false)
                            idLoadingAnimation.progress = 0f
                            idLoadingAnimation.visibility = View.GONE
                        }

                        override fun onAnimationCancel(p0: Animator?) {
                        }

                        override fun onAnimationStart(p0: Animator?) {

                        }

                    })
                }
                SocketActionType.FACE_GUIDE_ON.type->{
                    faceLoadingAnimation.visibility = View.VISIBLE
                    viewModel.toogleFaceGuideStatusChanged(true)
                }
                SocketActionType.FACE_GUIDE_OFF.type->{
                    faceLoadingAnimation.setAnimation(R.raw.face)
                    faceLoadingAnimation.repeatCount = 0
                    faceLoadingAnimation.playAnimation()
                    faceLoadingAnimation.addAnimatorListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {
                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            viewModel.toogleFaceGuideStatusChanged(false)
                            faceLoadingAnimation.progress = 0f
                            faceLoadingAnimation.visibility = View.GONE
                        }

                        override fun onAnimationCancel(p0: Animator?) {
                        }

                        override fun onAnimationStart(p0: Animator?) {

                        }

                    })

                }
            }
        }

        observe(viewModel.errorData) {
            when(it){
                is ApiError ->{
                    it.message?.get(0)?.let { it1 -> Toasty.error(requireContext(), it1,Toast.LENGTH_SHORT).show() }
                }
                else -> {
                    errorProccess()
                }
            }

        }
        observe(viewModel.stateData){
            when(it){
                is State.Loading->{
                    mBottomSheetDialog?.let {
                        tanView?.findViewById<ProgressBar>(R.id.progressCircular)?.visibility = View.VISIBLE
                        tanView?.findViewById<ProgressBar>(R.id.progressCircular)?.isEnabled = false
                    }
                }
                is State.Loaded->{
                    mBottomSheetDialog?.let {
                        tanView?.findViewById<ProgressBar>(R.id.progressCircular)?.visibility = View.GONE
                        tanView?.findViewById<ProgressBar>(R.id.progressCircular)?.isEnabled = true
                    }
                }
            }
        }

        observe(viewModel.socketStatus){
            when(it){
                SocketConnectionStatus.CLOSE->{
                    errorProccess()
                }
                SocketConnectionStatus.EXCEPTION->{
                    errorProccess()
                }
            }
        }

        observe(viewModel.tanResponse){
            mBottomSheetDialog?.let {
                it.dismiss()

            }
        }

         observe(viewModel.tanError){
          if (it){
              Toasty.error(requireContext(),getString(R.string.enter_pin),Toasty.LENGTH_SHORT).show()
           }
         }


    }

    fun errorProccess(){
        Toasty.error(requireContext(),getString(R.string.connection_error_when_calling),Toast.LENGTH_LONG).show()
        goCallWaitFragmentFromFail()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            onFragmentTransactionListener = activity as OnFragmentTransactionListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }



    override fun onPause() {
        super.onPause()
        handler?.removeCallbacks(runnable)
        handler = null
        runnable = null
        mBottomSheetDialog?.let {
            mBottomSheetDialog?.dismiss()
        }
        viewModel.closeStream()
        surfaceViewRendererLocal.release()
        surfaceViewRendererRemote.release()
        rootEglBase?.release()
        rootEglBase = null
       if (viewModel.callStarted && !viewModel.goThankYouFragment){
           viewModel.finishCall()
           goCallWaitFragmentFromFail()
       }
    }


    override fun onDetach() {
        super.onDetach()
        onFragmentTransactionListener = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.startRtcProccessing()
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            StartedCallFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun showBottomSheetDialog() {
        tanView = layoutInflater.inflate(R.layout.fragment_bottom_tan, null,false)
         mBehavior  =  BottomSheetBehavior.from(bottomSheetContainer)
        mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        mBehavior?.skipCollapsed = true
         mBottomSheetDialog  = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomSheetDialog?.behavior?.isDraggable = false
        mBottomSheetDialog?.behavior?.isHideable = false
        mBottomSheetDialog?.setCancelable(false)
        mBottomSheetDialog?.setCanceledOnTouchOutside(false)
         tanView?.rootView?.let { mBottomSheetDialog?.setContentView(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        mBottomSheetDialog?.show()
        val pin1 = tanView?.findViewById<EditText>(R.id.etPin1)
        val pin2 = tanView?.findViewById<EditText>(R.id.etPin2)
        val pin3 = tanView?.findViewById<EditText>(R.id.etPin3)
        val pin4 = tanView?.findViewById<EditText>(R.id.etPin4)
        val pin5 = tanView?.findViewById<EditText>(R.id.etPin5)
        val pin6 = tanView?.findViewById<EditText>(R.id.etPin6)
        val sendBtn = tanView?.findViewById<CardView>(R.id.relLaySendSmsCode)
        pin1?.changed { pin2?.requestFocus() }
        pin2?.changed { pin3?.requestFocus() }
        pin3?.changed { pin4?.requestFocus() }
        pin4?.changed { pin5?.requestFocus() }
        pin5?.changed { pin6?.requestFocus() }
        pin1?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin1) else onFinishFocusEditText(pin1)
        }
        pin2?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin2) else onFinishFocusEditText(pin2)
        }
        pin3?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin3) else onFinishFocusEditText(pin3)
        }
        pin4?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin4) else onFinishFocusEditText(pin4)
        }
        pin5?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin5) else onFinishFocusEditText(pin5)
        }
        pin6?.setOnFocusChangeListener { _, b ->
            if (b) onStartFocusEditText(pin6) else onFinishFocusEditText(pin6)
        }
        sendBtn?.setOnClickListener {
            viewModel.tanCode = pin1?.text.toString().trim()+pin2?.text.toString().trim()+pin3?.text.toString().trim()+pin4?.text.toString().trim()+pin5?.text.toString().trim()+pin6?.text.toString().trim()
            viewModel.setSmsCode()
        }
        mBottomSheetDialog?.setOnDismissListener(DialogInterface.OnDismissListener {
            mBottomSheetDialog = null
            tanView = null
            mBehavior = null
        })
    }

    fun onStartFocusEditText(edt : EditText){
        edt.alpha = 1f
        edt.background = ContextCompat.getDrawable(requireContext(),R.drawable.all_yellow_border_white_bg)
    }

    fun onFinishFocusEditText(edt : EditText){
        edt.alpha = 0.5f
        edt.background = ContextCompat.getDrawable(requireContext(),R.drawable.all_grey_border_white_bg)
    }




    override fun getLayoutId(): Int = R.layout.fragment_started_call

    //endregion
}