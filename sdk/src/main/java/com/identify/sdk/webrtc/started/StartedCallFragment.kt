package com.identify.sdk.webrtc.started

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.ApiError
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.base.State
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SdpType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.sure.AreYouSureDialogFragment
import es.dmoral.toasty.Toasty
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
            onFragmentTransactionListener?.onRemoveStartedCallFragment()
        }


        onBackPressClicked()



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
        surfaceViewRendererLocal.setMirror(true)
        surfaceViewRendererLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        surfaceViewRendererRemote.init(rootEglBase?.eglBaseContext, null)
        surfaceViewRendererRemote.setEnableHardwareScaler(false)
        surfaceViewRendererRemote.setMirror(true)
        surfaceViewRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
    }

    override fun onStop() {
        super.onStop()
        mBottomSheetDialog?.let {
            mBottomSheetDialog?.dismiss()
        }
        viewModel.closeStream()
        surfaceViewRendererLocal.release()
        surfaceViewRendererRemote.release()
        rootEglBase?.release()
        rootEglBase = null
    }





     private fun observeDataChanges() {
        observe(viewModel.successData){
            when(it.action){
                SocketActionType.TERMINATE_CALL.type->{
                    onFragmentTransactionListener?.onRemoveStartedCallFragment()
                }
                SocketActionType.IM_ONLINE.type -> {
                    // Toast.makeText(context,getString(R.string.customer_service_online), Toast.LENGTH_LONG).show()
                }
                SocketActionType.IM_OFFLINE.type -> {
                    onFragmentTransactionListener?.onRemoveStartedCallFragment()
                    // Toast.makeText(context,getString(R.string.customer_service_offline), Toast.LENGTH_LONG).show()
                }
                SocketActionType.END_CALL.type->{
                    onFragmentTransactionListener?.onRemoveStartedCallFragment()
                }
                SocketActionType.SUBREJECTED.type->{
                    onFragmentTransactionListener?.onRemoveStartedCallFragment()
                }
                SocketActionType.SDP.type->{

                }
                SdpType.ANSWER.type->{
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


    }

    fun errorProccess(){
        Toasty.error(requireContext(),getString(R.string.connection_error_when_calling),Toast.LENGTH_LONG).show()
        onFragmentTransactionListener?.onRemoveStartedCallFragment()
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
        if (mBehavior?.state === BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
         mBottomSheetDialog  = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
         tanView?.rootView?.let { mBottomSheetDialog?.setContentView(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        mBottomSheetDialog?.show()
        (tanView?.findViewById<RelativeLayout>(R.id.relLaySendSmsCode))?.setOnClickListener {
            viewModel.tanCode = (tanView?.findViewById<EditText>(R.id.etPin))?.text.toString()
            viewModel.setSmsCode()
        }
        mBottomSheetDialog?.setOnDismissListener(DialogInterface.OnDismissListener {
            mBottomSheetDialog = null
            tanView = null
            mBehavior = null
        })
    }




    override fun getLayoutId(): Int = R.layout.fragment_started_call

    //endregion
}