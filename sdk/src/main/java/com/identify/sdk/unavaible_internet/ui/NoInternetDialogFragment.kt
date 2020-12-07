package com.identify.sdk.unavaible_internet.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import com.identify.sdk.R
import com.identify.sdk.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_no_internet.*


class NoInternetDialogFragment  : BaseDialogFragment() {


    private var listener : NoInternetClickInterface ?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryAgainBtn.setOnClickListener {
            listener?.noInternetClickListener()
        }

    }


    interface NoInternetClickInterface{
        fun noInternetClickListener()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is NoInternetClickInterface) {
            listener = activity as NoInternetClickInterface
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }






    companion object {

        @JvmStatic
        fun newInstance() =
            NoInternetDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun getLayoutId(): Int  = R.layout.dialog_no_internet

}