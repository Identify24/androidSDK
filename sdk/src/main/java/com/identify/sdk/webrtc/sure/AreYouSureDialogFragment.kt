package com.identify.sdk.webrtc.sure

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.identify.sdk.R
import com.identify.sdk.base.BaseDialogFragment
import kotlinx.android.synthetic.main.are_you_sure_dialog.*


class AreYouSureDialogFragment : BaseDialogFragment() {


    private var listener : AreYouSureListenerInterface ?=null

    interface AreYouSureListenerInterface{
        fun onHandleSureData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        yesBtn.setOnClickListener {
            listener?.onHandleSureData()
        }
        noBtn.setOnClickListener {
            dismiss()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is AreYouSureListenerInterface) {
            listener = parentFragment as AreYouSureListenerInterface
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }


    override fun onResume() {
        super.onResume()

        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_DOWN){
                    true
                }
                else{
                    dismiss()
                    true
                }


            } else
                false
        }
    }






    override fun getLayoutId(): Int  = R.layout.are_you_sure_dialog

    companion object {


        @JvmStatic
        fun newInstance() =
            AreYouSureDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}