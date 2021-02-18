package com.identify.sdk.face

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.KeyEvent
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.hsalf.smilerating.BaseRating
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.IdentifyResultListener
import com.identify.sdk.R
import com.identify.sdk.SdkApp.identityOptions
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.face.facedetector.MLKitFaceProcessorListener
import com.identify.sdk.face.facedetector.MLKitFacesAnalyzer
import com.identify.sdk.face.information.FaceInformationDialogFragment
import com.identify.sdk.repository.model.enums.FaceDetectionProcessType
import com.identify.sdk.repository.model.enums.IdentityType
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_face_detection.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@ExperimentalCoroutinesApi
class FaceDetectionFragment : BaseFragment(),
    FaceInformationDialogFragment.FaceInformationStatusListener {


    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    var detector : FaceDetector ?= null

    var faceAnalysis : MLKitFacesAnalyzer ?= null

    private lateinit var cameraExecutor: ExecutorService

    private var onFragmentTransactionListener: OnFragmentTransactionListener?= null

    private var identifyResultListener : IdentifyResultListener?= null

    private val callViewModel: CallViewModel by activityViewModels()

    private val viewModel by viewModels<FaceDetectionViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        observeDataChanges()
        onBackPressClicked()

    }

    private fun onBackPressClicked(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                return@OnKeyListener true
            }
            false
        })
    }


    private fun startFaceCallback(facesAnalyzer : MLKitFacesAnalyzer){
        facesAnalyzer.faceProcessorListener = object :MLKitFaceProcessorListener{
            override fun success(face: MutableList<Face>) {
                for (fc in face){
                  faceStatusSetter(fc)
                }

            }

        }
    }

    fun openInformationDialog(anim : Int, text : String){
        val fragment = FaceInformationDialogFragment.newInstance(anim,text)
        fragment.show(childFragmentManager,FaceInformationDialogFragment::class.java.toString())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview

            val preview = Preview.Builder()
                    .setTargetResolution( Size(preview_view.width, preview_view.height))
                    .build()
                    .also {
                        it.setSurfaceProvider(preview_view.createSurfaceProvider())
                    }
            val faceDetectorOptions =
                FaceDetectorOptions.Builder()
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setMinFaceSize(0.1f)
                    .build()
            val detector = FaceDetection.getClient(faceDetectorOptions)
            val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(preview_view.width, preview_view.height))
                    .build()
                    .also {
                        faceAnalysis =  MLKitFacesAnalyzer(detector,preview_view,face_image_view)
                        faceAnalysis?.let { analysis ->
                            startFaceCallback(analysis)
                            it.setAnalyzer(cameraExecutor,analysis)
                        }
                    }

            // Select back camera as a default
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview,imageAnalyzer)

            } catch(exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


     override fun onResume() {
        super.onResume()
        startCamera()
    }

    override fun onPause() {
        super.onPause()
        detector?.close()
        detector = null
        faceAnalysis?.faceProcessorListener = null
        faceAnalysis = null

    }

     override fun onDestroy() {
        super.onDestroy()
         detector?.close()
         detector = null
         faceAnalysis?.faceProcessorListener = null
         faceAnalysis = null
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            onFragmentTransactionListener = activity as OnFragmentTransactionListener
            identifyResultListener = activity as IdentifyResultListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentTransactionListener = null
        identifyResultListener = null
    }




    private fun observeDataChanges() {
        observe(viewModel.errorData){
           Toasty.error(requireContext(),getString(it.messageRes),Toasty.LENGTH_SHORT).show()
            viewModel.errorFoundRetryAllProcess()
            tvFaceStatus.text = getString(R.string.smiling_text)

        }



    }

    fun goOpenWaitFragment(){
        onFragmentTransactionListener?.onRemoveOcrFragment()
        onFragmentTransactionListener?.onRemoveNfcFragment()
        onFragmentTransactionListener?.onRemoveFaceDetectionFragment()
        onFragmentTransactionListener?.onOpenWaitFragment()
    }





    companion object {

        @JvmStatic
        fun newInstance() =
            FaceDetectionFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }



    override fun getLayoutId(): Int = R.layout.fragment_face_detection


     fun faceStatusSetter(
        face: Face,
    ) {
         when(viewModel.activeFaceDetectionProcessType){
             FaceDetectionProcessType.SMILING ->{
                 var smile = 0
                 if (face.smilingProbability  != null){
                     if (face.smilingProbability > .8) {
                         smile = BaseRating.GREAT
                         smilingFinished()
                     } else if (face.smilingProbability <= .8 && face.smilingProbability > .6) {
                         smile = BaseRating.GOOD
                     } else if (face.smilingProbability <= .6 && face.smilingProbability > .4) {
                         smile = BaseRating.OKAY
                     } else if (face.smilingProbability <= .4 && face.smilingProbability > .2) {
                         smile = BaseRating.BAD
                     }
                     smile_rating.setSelectedSmile(smile, true)
                 }
             }
             FaceDetectionProcessType.BLINK ->{
                 if (face.leftEyeOpenProbability != null && face.leftEyeOpenProbability < 0.4) {
                     viewModel.leftEyesClosed = true
                 }
                 if (face.rightEyeOpenProbability != null && face.rightEyeOpenProbability < 0.4) {
                     viewModel.rightEyesClosed = true
                 }
                 if (viewModel.rightEyesClosed && viewModel.leftEyesClosed){
                     eyesFinished()
                 }
             }
             FaceDetectionProcessType.HEAD_RIGHT ->{
                 if (face.headEulerAngleY < -45f ){
                     headRightFinished()
                 }
             }
             FaceDetectionProcessType.HEAD_LEFT ->{
                 if (face.headEulerAngleY > 45f ){
                     headLeftFinished()
                 }
             }
         }

         println("headEulerAngleX = "+face.headEulerAngleX)
         println("headEulerAngleY = "+face.headEulerAngleY)
         println("headEulerAngleZ = "+face.headEulerAngleZ)


    }


    private fun headRightFinished(){
        faceDetectionSetActiveProcess(FaceDetectionProcessType.NOT_AVAILABLE)
        faceDetectionSetFinishedProcess(FaceDetectionProcessType.HEAD_RIGHT)
        animationStart()
    }

    private fun headLeftFinished(){
        faceDetectionSetActiveProcess(FaceDetectionProcessType.NOT_AVAILABLE)
        faceDetectionSetFinishedProcess(FaceDetectionProcessType.HEAD_LEFT)
        animationStart()
    }

    private fun eyesFinished(){
        faceDetectionSetActiveProcess(FaceDetectionProcessType.NOT_AVAILABLE)
        faceDetectionSetFinishedProcess(FaceDetectionProcessType.BLINK)
        animationStart()
    }


    private fun smilingFinished(){
        faceDetectionSetActiveProcess(FaceDetectionProcessType.NOT_AVAILABLE)
        faceDetectionSetFinishedProcess(FaceDetectionProcessType.SMILING)
        smile_rating.isEnabled = false
        animationStart()
    }

    private fun faceDetectionSetFinishedProcess(process : FaceDetectionProcessType){
        viewModel.faceDetectionFinishedProcessType = process
    }
    private fun faceDetectionSetActiveProcess(process : FaceDetectionProcessType){
        viewModel.activeFaceDetectionProcessType = process
    }

    private fun animationStart(){
        successStatusAnimation.visibility = View.VISIBLE
        successStatusAnimation.setAnimation(R.raw.success)
        successStatusAnimation.repeatCount = 0
        successStatusAnimation.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                when(viewModel.faceDetectionFinishedProcessType){
                    FaceDetectionProcessType.SMILING ->{
                        callViewModel.customerInformationEntity?.customerUid?.let { room ->
                            viewModel.sendSmiling(room)
                        }
                        openInformationDialog(R.raw.blink_couple,getString(R.string.blink_text))
                        tvFaceStatus.text = getString(R.string.blink_text)
                    }
                    FaceDetectionProcessType.BLINK ->{
                        openInformationDialog(R.raw.look_right,getString(R.string.turn_your_head_right_text))
                        tvFaceStatus.text = getString(R.string.turn_your_head_right_text)
                    }
                    FaceDetectionProcessType.HEAD_RIGHT ->{
                        openInformationDialog(R.raw.look_left,getString(R.string.turn_your_head_left_text))
                        tvFaceStatus.text = getString(R.string.turn_your_head_left_text)
                    }
                    FaceDetectionProcessType.HEAD_LEFT ->{
                        lifecycleScope.launch {
                            when(identityOptions?.getIdentityType()){
                                IdentityType.WITHOUT_CALL->{
                                    tvFaceStatus.text = getString(R.string.finish_process)
                                    identifyResultListener?.vitalityDetectionProcessFinished()
                                }
                                IdentityType.FULL_PROCESS->{
                                    tvFaceStatus.text = getString(R.string.redirect_to_customer_services)
                                    delay(2000)
                                    goOpenWaitFragment()
                                }
                            }

                        }

                    }
                }
                successStatusAnimation.removeAllAnimatorListeners()
                successStatusAnimation.visibility = View.GONE

            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })
        successStatusAnimation.playAnimation()

    }

    override fun continueFaceDetection() {
        Handler().postDelayed({
            when(viewModel.faceDetectionFinishedProcessType){
                FaceDetectionProcessType.SMILING ->{
                    faceDetectionSetActiveProcess(FaceDetectionProcessType.BLINK)
                }
                FaceDetectionProcessType.BLINK ->{
                    faceDetectionSetActiveProcess(FaceDetectionProcessType.HEAD_RIGHT)
                }
                FaceDetectionProcessType.HEAD_RIGHT ->{
                    faceDetectionSetActiveProcess(FaceDetectionProcessType.HEAD_LEFT)
                }
            }
        }, 1500)

    }


}