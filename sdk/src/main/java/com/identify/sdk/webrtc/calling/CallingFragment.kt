package com.identify.sdk.webrtc.calling

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_calling.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class CallingFragment : BaseFragment()   {


    //region Properties


    private val viewModel: CallViewModel by activityViewModels()

    private var onFragmentTransactionListener: OnFragmentTransactionListener?= null

    //endregion





    //region Functions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDataChanges()

        buttonRejectCall.setOnClickListener {
            onFragmentTransactionListener?.onRemoveCallingFragment()
        }
        buttonAcceptCall.setOnClickListener {
            onFragmentTransactionListener?.onRemoveCallingFragment()
            onFragmentTransactionListener?.onOpenStartedCallFragment()
        }


        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                onFragmentTransactionListener?.onRemoveCallingFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)


    }





    companion object {

        @JvmStatic
        fun newInstance() =
            CallingFragment().apply {

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



     private fun observeDataChanges() {1

         observe(viewModel.successData){
            when(it.action){
                SocketActionType.TERMINATE_CALL.type->{
                    onFragmentTransactionListener?.onRemoveCallingFragment()
                }
                SocketActionType.IM_ONLINE.type -> {
                    Toast.makeText(context,getString(R.string.customer_service_online), Toast.LENGTH_LONG).show()
                }
                SocketActionType.IM_OFFLINE.type -> {
                    onFragmentTransactionListener?.onRemoveCallingFragment()
                    Toast.makeText(context,getString(R.string.customer_service_offline), Toast.LENGTH_LONG).show()
                }
                SocketActionType.END_CALL.type->{
                    onFragmentTransactionListener?.onRemoveCallingFragment()
                }
            }
        }

        observe(viewModel.errorData) {
            errorProccess()
        }

        observe(viewModel.socketStatus){
            //errorProccess()
            println("soket durumu  =  "+it.type)
        }



    }


    fun errorProccess(){
        Toasty.error(requireContext(),getString(R.string.connection_error_when_calling),Toast.LENGTH_LONG).show()
        onFragmentTransactionListener?.onRemoveCallingFragment()
    }





    override fun onResume() {
        super.onResume()
        viewModel.connectSocket()
    }



    override fun getLayoutId(): Int = R.layout.fragment_calling

    //endregion
}