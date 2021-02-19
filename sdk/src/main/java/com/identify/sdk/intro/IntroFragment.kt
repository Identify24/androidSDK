package com.identify.sdk.intro

import android.content.Context
import android.os.Bundle
import android.view.View
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.webrtc.OnFragmentTransactionListener


class IntroFragment : BaseFragment() {

    private var onFragmentTransactionListener: OnFragmentTransactionListener?= null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataChanges()
    }


    override fun getLayoutId(): Int  = R.layout.fragment_mrz



    private fun observeDataChanges() {

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

    companion object {

        @JvmStatic
        fun newInstance() =
            IntroFragment()
    }



}