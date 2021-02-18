package com.identify.sdk.webrtc.wait

import android.R.attr
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
import com.identify.sdk.repository.network.ApiImpl
import com.identify.sdk.repository.soket.RtcConnectionSource
import com.identify.sdk.repository.soket.SocketSource
import com.identify.sdk.util.observe
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.sure.AreYouSureDialogFragment
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_waiting_call.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class CallWaitingFragment : BaseFragment(),
    AreYouSureDialogFragment.AreYouSureListenerInterface {


    //region Properties

    private var sureDialogFragment : AreYouSureDialogFragment ?= null

    private val viewModel: CallViewModel by activityViewModels()

    private var onFragmentTransactionListener: OnFragmentTransactionListener ?= null


    //endregion


    //region Functions


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       init()

        observeDataChanges()

        viewModel.observeSocketStatus()

        viewTitle.ivBack.setOnClickListener {
            showSureFragment()
        }

        onBackPressClicked()

        btnReConnect.setOnClickListener {
                btnReConnect.isEnabled = false
                viewModel.nfcStatusType?.let { it1 -> viewModel.connectSocket(false, it1) }
                Toasty.info(requireContext(),"Lütfen Bekleyin",Toast.LENGTH_SHORT).show()
        }

    }


    fun onBackPressClicked(){
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
                showSureFragment()
                return@OnKeyListener true
            }
            false
        })
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



    private fun init(){
        arguments?.let {
            val customer =  it.getParcelable<CustomerInformationEntity>("customer")
            customer?.let {
                    viewModel.customerInformationEntity = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.nfcStatusType?.let { viewModel.connectSocket(false, it) }
    }








    private fun showSureFragment(){
        if (sureDialogFragment == null) sureDialogFragment =  AreYouSureDialogFragment.newInstance()
        sureDialogFragment?.let {
         if (!it.isAdded) childFragmentManager.beginTransaction().add(it,AreYouSureDialogFragment::class.java.toString()).commitAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sureDialogFragment?.let {
            it.dismissAllowingStateLoss()
        }
        sureDialogFragment = null
    }


    companion object {

        @JvmStatic
        fun newInstance(customer : CustomerInformationEntity) =
            CallWaitingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("customer", customer)
                }
            }
    }

    private fun openCallingFragment(){
        onFragmentTransactionListener?.onOpenCallingFragment()
        onFragmentTransactionListener?.onRemoveWaitFragment()
    }


     private fun observeDataChanges() {
        observe(viewModel.successData){
            when(it.action){
                SocketActionType.INIT_CALL.type->{
                    openCallingFragment()
                }
            }
        }

        observe(viewModel.errorData) {
            linLayConnectionLost.visibility = View.VISIBLE
            linLayConnectionSuccess.visibility = View.GONE
            btnReConnect.isEnabled = true
        }

        observe(viewModel.socketStatus){
            when(it){
                SocketConnectionStatus.OPEN->{
                    btnReConnect.isEnabled = true
                    linLayConnectionSuccess.visibility = View.VISIBLE
                    linLayConnectionLost.visibility = View.GONE

                }
                SocketConnectionStatus.CLOSE->{
                    linLayConnectionLost.visibility = View.VISIBLE
                    linLayConnectionSuccess.visibility = View.GONE
                    btnReConnect.isEnabled = true

                }
                SocketConnectionStatus.EXCEPTION->{
                    linLayConnectionLost.visibility = View.VISIBLE
                    linLayConnectionSuccess.visibility = View.GONE
                    Toasty.error(requireContext(),getString(R.string.reason_network),Toast.LENGTH_SHORT).show()
                    btnReConnect.isEnabled = true
                }
            }
        }

    }


    override fun getLayoutId(): Int = R.layout.fragment_waiting_call


    override fun onHandleSureData() {
        sureDialogFragment?.let {
            it.dismiss()
        }
        viewModel.disconnectSocket()
        activity?.finish()

    }


    //endregion

}