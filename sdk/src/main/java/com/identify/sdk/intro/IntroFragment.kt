package com.identify.sdk.intro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.identify.sdk.IdentifyActivity
import com.identify.sdk.R
import com.identify.sdk.SdkApp.identityOptions
import com.identify.sdk.base.BaseFragment
import com.identify.sdk.repository.model.enums.IdentityType
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import kotlinx.android.synthetic.main.fragment_intro.*


class IntroFragment : BaseFragment() {

    private var onFragmentTransactionListener: OnFragmentTransactionListener?= null

    private var introListener: IntroListener?= null

    var viewPagerPosition = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createViewPagerData()
        cardStart.setOnClickListener {
            relLayWelcome.visibility = View.GONE
            relLaySlider.visibility = View.VISIBLE
        }

        relLayNext.setOnClickListener {
            when(viewPagerPosition){
                4->{
                    if (checkPermissionGranted()) {
                        introListener?.onCheckPermission()
                    }else {
                        relLaySlider.visibility = View.GONE
                        relLayPermission.visibility = View.VISIBLE
                    }
                }
                1->{
                    if (identityOptions?.getIdentityType() == IdentityType.ONLY_CALL){
                        if (checkPermissionGranted()) {
                            introListener?.onCheckPermission()
                        }else {
                            relLaySlider.visibility = View.GONE
                            relLayPermission.visibility = View.VISIBLE
                        }
                    }else{
                        viewpager.currentItem = viewPagerPosition+1
                    }
                }

                2->{
                    if (identityOptions?.getIdentityType() == IdentityType.WITHOUT_CALL){
                        if (checkPermissionGranted()) {
                            introListener?.onCheckPermission()
                        }else {
                            relLaySlider.visibility = View.GONE
                            relLayPermission.visibility = View.VISIBLE
                        }
                    }else{
                        viewpager.currentItem = viewPagerPosition+1
                    }
                }

                else->{
                    viewpager.currentItem = viewPagerPosition+1
                }

            }
        }
        relLayBack.setOnClickListener {
            when(viewPagerPosition){
                0->{
                    if (checkPermissionGranted()) {
                        introListener?.onCheckPermission()
                    }else {
                        relLaySlider.visibility = View.GONE
                        relLayPermission.visibility = View.VISIBLE
                    }
                }
                else->{
                    viewpager.currentItem = viewPagerPosition-1
                }

            }
        }
        cardPermission.setOnClickListener {
            introListener?.onCheckPermission()
        }


        onBackPressClicked()
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


    private fun createViewPagerData() {
        val list : MutableList<Pair<Drawable?,String>>  = mutableListOf()
         when(identityOptions?.getIdentityType()){
                           IdentityType.ONLY_CALL->{
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_customer_representative_illustration),getString(R.string.intro_call_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_tan_illustration),getString(R.string.intro_tan_desc)))
                           }
                           IdentityType.WITHOUT_CALL->{
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_ocr_illustration),getString(R.string.intro_ocr_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_nfc_illustration),getString(R.string.intro_nfc_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_vitality_illustration),getString(R.string.intro_vitality_desc)))
                           }
                           IdentityType.FULL_PROCESS->{
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_ocr_illustration),getString(R.string.intro_ocr_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_nfc_illustration),getString(R.string.intro_nfc_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_vitality_illustration),getString(R.string.intro_vitality_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_customer_representative_illustration),getString(R.string.intro_call_desc)))
                               list.add(Pair(ContextCompat.getDrawable(requireContext(),R.drawable.ic_tan_illustration),getString(R.string.intro_tan_desc)))
                           }
                       }
        setupViewPager(list)
    }


    fun checkPermissionGranted()  : Boolean{
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED) return true
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO)  == PackageManager.PERMISSION_GRANTED) return true
        return false
    }

    private fun setupViewPager(list: MutableList<Pair<Drawable?, String>>) {
        viewpager.offscreenPageLimit = 1
        viewpager.adapter =  IntroAdapter(list)
        viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewpager.isUserInputEnabled = false
        TabLayoutMediator(tabLayout, viewpager) { _, _ ->
        }.attach()


        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
                when(position){
                    0->{
                        introFirstStatus()
                    }
                    1->{
                        if (identityOptions?.getIdentityType() == IdentityType.ONLY_CALL){
                            tvNext.text = getString(R.string.go_on)
                            tvSkipBack.text = getString(R.string.back)
                            tvNext.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkYellow))
                            imgRightArrow.visibility = View.GONE
                        }else{
                            introStatusNormal()
                        }
                    }
                    2->{
                        if (identityOptions?.getIdentityType() == IdentityType.WITHOUT_CALL){
                            tvNext.text = getString(R.string.go_on)
                            tvNext.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkYellow))
                            imgRightArrow.visibility = View.GONE
                        }
                        else{
                            introStatusNormal()
                        }
                    }
                    4->{
                        tvNext.text = getString(R.string.go_on)
                        tvNext.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDarkYellow))
                        imgRightArrow.visibility = View.GONE
                    }
                    else->{
                        introStatusNormal()
                    }

                }
                super.onPageSelected(position)

            }
        })
    }

    fun introStatusNormal(){
        tvNext.text = getString(R.string.next)
        tvSkipBack.text = getString(R.string.back)
        imgLeftArrow.visibility = View.VISIBLE
        imgRightArrow.visibility = View.VISIBLE
        tvNext.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
    }

    fun introFirstStatus(){
        tvNext.text = getString(R.string.next)
        tvSkipBack.text = getString(R.string.skip)
        imgLeftArrow.visibility = View.GONE
        imgRightArrow.visibility = View.VISIBLE
        tvNext.setTextColor(ContextCompat.getColor(requireContext(),android.R.color.white))
    }


    override fun getLayoutId(): Int  = R.layout.fragment_intro






    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IdentifyActivity) {
            onFragmentTransactionListener = activity as OnFragmentTransactionListener
            introListener = activity as IntroListener
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentTransactionListener = null
        introListener = null
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            IntroFragment()
    }



}