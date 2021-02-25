package com.identify.sdk.mrz


import android.content.Context
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.IdentifyResultListener
import com.identify.sdk.R
import com.identify.sdk.SdkApp.identityOptions
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.NfcStatusType
import com.identify.sdk.util.alert
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import kotlinx.android.synthetic.main.app_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_nfc.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec
import org.jmrtd.lds.icao.MRZInfo

@ExperimentalCoroutinesApi
class NfcFragment : BaseFragment() {

    private val viewModel by viewModels<NfcViewModel>()

    private val callViewModel: CallViewModel by activityViewModels()

    var finishedNfcProcess : () -> Unit = { }


    private var onFragmentTransactionListener: OnFragmentTransactionListener ?= null

    private var  identifyResultListener : IdentifyResultListener ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.mrzInfo = it.getSerializable("mrzInfo") as MRZInfo
            viewModel.customer = it.getParcelable("customer")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataChanges()
        onBackPressClicked()
    }

    private fun onBackPressClicked(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                onFragmentTransactionListener?.onRemoveNfcFragment()
                onFragmentTransactionListener?.onOpenOcrFragment()
                return@OnKeyListener true
            }
            false
        })
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            onFragmentTransactionListener = activity as OnFragmentTransactionListener
            identifyResultListener  = activity as IdentifyResultListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentTransactionListener = null
        identifyResultListener = null
    }




    fun triggerNfcReader(isoDep: IsoDep){
        startNfcReading()
        val bacKey: BACKeySpec = BACKey(viewModel.mrzInfo?.documentNumber, viewModel.mrzInfo?.dateOfBirth, viewModel.mrzInfo?.dateOfExpiry)
        viewModel.getNfcData(isoDep,bacKey)
    }

    private fun startNfcReading() {
        relLayDefaultNfc.visibility = View.GONE
        linLayReadNfc.visibility = View.VISIBLE
        nfcStatusTv.text = getString(R.string.nfc_reading)
        nfcAnimation.setAnimation(R.raw.nfc_reading)
        nfcAnimation.repeatCount = LottieDrawable.INFINITE
        nfcAnimation.playAnimation()
    }
    private fun finishedNfcReading(isSuccess : Boolean) {
       when(isSuccess){
           true->{
               nfcAnimation.setAnimation(R.raw.nfc_success)
               nfcStatusTv.text = getString(R.string.nfc_success)
           }
           false->{
               nfcAnimation.setAnimation(R.raw.nfc_fail)
               nfcStatusTv.text = getString(R.string.nfc_try_again)
           }
       }
        nfcAnimation.repeatCount = 0
        nfcAnimation.playAnimation()
    }


     fun observeDataChanges() {
        observe(viewModel.nfcApiResult){
            if (it){
                finishedNfcProcess()
                finishedNfcReading(true)
                viewModel.nfcStatusType = NfcStatusType.SUCCESS
                viewModel.nfcStatusSendFromSocket(NfcStatusType.SUCCESS)
            }
            else{
                nfcFailProcess()
            }

        }
        observe(viewModel.errorMrz){
            nfcFailProcess()
        }

         observe(viewModel.socketResponse){
             when(it.action){
                 SocketActionType.MESSAGE_SENDED.type->{
                     when(viewModel.nfcStatusType){
                         NfcStatusType.SUCCESS->{
                             viewModel.mrzDto?.let { mrz ->
                                 identifyResultListener?.nfcProcessFinished(true,
                                     mrz
                                 )
                             }
                         }
                         NfcStatusType.FAILURE->{
                             viewModel.mrzDto?.let { mrz ->
                                 identifyResultListener?.nfcProcessFinished(false,
                                     null
                                 )
                             }
                         }
                     }
                     lifecycleScope.launch {
                         delay(3000)
                         gotoFaceDetectionFragment()
                     }
                 }
             }
         }

         observe(callViewModel.successData){
             when(it.action){
                 SocketActionType.SKIPNFC.type->{
                     gotoFaceDetectionFragment()
                 }
             }
         }


    }


    private fun gotoFaceDetectionFragment(){
        onFragmentTransactionListener?.onRemoveNfcFragment()
        onFragmentTransactionListener?.onRemoveOcrFragment()
        onFragmentTransactionListener?.onOpenFaceDetectionFragment()
    }


    private fun nfcFailProcess(){
        Toast.makeText(requireContext(),getString(R.string.nfc_toast_message),Toast.LENGTH_SHORT).show()
        finishedNfcProcess()
        finishedNfcReading(false)
        checkErrorCount()
    }

    fun checkErrorCount(){
        if (viewModel.nfcExceptionCounter >= identityOptions?.getNfcExceptionCount()!!){
            viewModel.nfcStatusType = NfcStatusType.FAILURE
            viewModel.nfcStatusSendFromSocket(NfcStatusType.FAILURE)
            requireContext().alert(false, getString(R.string.go_on),null,getString(R.string.nfc_error_count_title),getString(R.string.nfc_error_count_desc),{ dialog ->
                dialog.dismiss()
            },{},{})
        }else{
            viewModel.nfcExceptionCounter = viewModel.nfcExceptionCounter + 1
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(mrzInfo: MRZInfo,customer: CustomerInformationEntity) =
                NfcFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("customer", customer)
                        putSerializable("mrzInfo",mrzInfo)
                    }
                }
    }



    override fun getLayoutId(): Int = R.layout.fragment_nfc


}