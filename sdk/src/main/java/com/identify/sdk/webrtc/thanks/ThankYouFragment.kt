package com.identify.sdk.webrtc.thanks

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.IdentifyResultListener
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ThankYouFragment : BaseFragment()   {


    //region Properties


    private var onFragmentTransactionListener: OnFragmentTransactionListener?= null

    private var identifyResultListener : IdentifyResultListener ?= null


    //endregion





    //region Functions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        onBackPressClicked()

        lifecycleScope.launch {
            delay(2000)
            identifyResultListener?.callProcessFinished()
        }

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






    companion object {

        @JvmStatic
        fun newInstance() =
            ThankYouFragment().apply {

            }
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











    override fun getLayoutId(): Int = R.layout.fragment_thank_you

    //endregion
}