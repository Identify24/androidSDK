package com.identify.sdk.webrtc.calling

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.enums.SocketConnectionStatus
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

    var mediaPlayer : MediaPlayer?= null

    //endregion





    //region Functions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDataChanges()

        setMediaPlayer()

       /* buttonRejectCall.setOnClickListener {
            onFragmentTransactionListener?.onRemoveCallingFragment()
        }*/
        buttonAcceptCall.setOnClickListener {
            setCallStartedFragmentDefaultArgument()
            goStartedCallFragment()

        }

        onBackPressClicked()

    }

    private fun setCallStartedFragmentDefaultArgument() {
        viewModel.callStarted = false
        viewModel.handlerWorked = false
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




    private fun setMediaPlayer(){
        mediaPlayer = MediaPlayer.create(requireContext(),R.raw.call_ring)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

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


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer = null
    }

    fun goCallWaitFragment(){
        onFragmentTransactionListener?.onOpenWaitFragment()
        onFragmentTransactionListener?.onRemoveCallingFragment()
    }
    fun goStartedCallFragment(){
        onFragmentTransactionListener?.onOpenStartedCallFragment()
        onFragmentTransactionListener?.onRemoveCallingFragment()
    }



     private fun observeDataChanges() {

         observe(viewModel.successData){
            when(it.action){
                SocketActionType.TERMINATE_CALL.type->{
                    goCallWaitFragment()
                }
            }
        }

        observe(viewModel.errorData) {
            errorProcess()
        }

         observe(viewModel.socketStatus){
             when(it){
                 SocketConnectionStatus.CLOSE->{
                     errorProcess()
                 }
                 SocketConnectionStatus.EXCEPTION->{
                     errorProcess()
                 }
             }
         }



    }


    private fun errorProcess(){
        Toasty.error(requireContext(),getString(R.string.connection_error_when_calling),Toast.LENGTH_LONG).show()
        goCallWaitFragment()
    }






    override fun getLayoutId(): Int = R.layout.fragment_calling

    //endregion
}