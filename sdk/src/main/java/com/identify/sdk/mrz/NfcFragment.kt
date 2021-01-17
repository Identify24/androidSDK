package com.identify.sdk.mrz


import android.content.Context
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.network.ApiImpl
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import kotlinx.android.synthetic.main.app_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_nfc.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec
import org.jmrtd.lds.icao.MRZInfo

class NfcFragment : BaseFragment() {

    private val viewModel by viewModels<NfcViewModel>()

    var finishedNfcProcess : () -> Unit = { }

    private var onFragmentTransactionListener: OnFragmentTransactionListener ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.mrzInfo = it.getSerializable("mrzInfo") as MRZInfo
            viewModel.customer = it.getParcelable("customer")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewTitle.ivBack.setOnClickListener {
            onFragmentTransactionListener?.onRemoveNfcFragment()
        }
        observeDataChanges()
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




    fun triggerNfcReader(isoDep: IsoDep){
        startNfcReading()
        val bacKey: BACKeySpec = BACKey(viewModel.mrzInfo?.documentNumber, viewModel.mrzInfo?.dateOfBirth, viewModel.mrzInfo?.dateOfExpiry)
        viewModel.getNfcData(isoDep,bacKey)
    }

    private fun startNfcReading() {
        linLayScannerReady.visibility = View.GONE
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
        observe(viewModel.errorData){
            nfcFailProcess()
        }
        observe(viewModel.nfcApiResult){
            if (it){
                finishedNfcProcess()
                finishedNfcReading(true)
                lifecycleScope.launch {
                    delay(3000)
                    onFragmentTransactionListener?.onRemoveNfcFragment()
                    onFragmentTransactionListener?.onRemoveOcrFragment()
                    onFragmentTransactionListener?.onOpenWaitFragment()
                }
            }
            else{
                nfcFailProcess()
            }

        }
        observe(viewModel.errorMrz){
            nfcFailProcess()
        }


    }
    private fun nfcFailProcess(){
        Toast.makeText(requireContext(),getString(R.string.nfc_toast_message),Toast.LENGTH_SHORT).show()
        finishedNfcProcess()
        finishedNfcReading(false)
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