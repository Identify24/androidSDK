package com.identify.sdk.form

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.webrtc.sure.AreYouSureDialogFragment
import kotlinx.android.synthetic.main.app_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_webview_form.*

class WebViewFormFragment : BaseFragment(), AreYouSureDialogFragment.AreYouSureListenerInterface {

    //region Properties

    var listener: WebViewFormListener? = null

    private var sureDialogFragment : AreYouSureDialogFragment ?= null

    //endregion

    //region Functions


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.settings.javaScriptEnabled = true
        webView.isNestedScrollingEnabled = true
        viewTitle.ivBack.setOnClickListener {
            listener?.onFailure()
        }
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, progress: Int) {
                if (progress < 100) {
                    if (this@WebViewFormFragment.isAdded) relLayLoading.visibility = View.VISIBLE
                }

                if (progress == 100) {
                    if (this@WebViewFormFragment.isAdded) relLayLoading.visibility = View.GONE
                }
            }

            override fun onCloseWindow(window: WebView?) {
                listener?.onFailure()
            }

        }

           webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val uri: Uri = Uri.parse(url)
                val isSuccess: String? = uri.getQueryParameter("success")

                if (isSuccess == "true" ) {
                    listener?.onSuccess()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                listener?.onFailure()
            }


        }

        arguments?.getString("webViewUrl")?.let {
            webView.loadUrl(it)
            webView.requestFocus()
        }

        onBackPressClicked()



    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            listener = activity as WebViewFormListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
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

    override fun onDestroyView() {
        webView.stopLoading()
        super.onDestroyView()
        dismissSureDialog()

    }

    fun dismissSureDialog(){
        sureDialogFragment?.let {
            it.dismissAllowingStateLoss()
            sureDialogFragment = null
        }
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun showSureFragment(){
        if (sureDialogFragment == null) sureDialogFragment =  AreYouSureDialogFragment.newInstance()
        sureDialogFragment?.let {
            childFragmentManager.beginTransaction().add(it, AreYouSureDialogFragment::class.java.toString()).commitAllowingStateLoss()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(webViewUrl: String) =
            WebViewFormFragment().apply {
                arguments = Bundle().apply {
                  putString("webViewUrl", webViewUrl)
                }
            }
    }

    override fun onHandleSureData() {
        dismissSureDialog()
        activity?.finish()
    }


    override fun getLayoutId(): Int = R.layout.fragment_webview_form


    //endregion

}