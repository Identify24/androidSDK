package com.identify.sdk

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.unavaible_internet.livedata.ConnectionLiveData
import com.identify.sdk.unavaible_internet.ui.NoInternetDialogFragment
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.calling.CallingFragment
import com.identify.sdk.webrtc.started.StartedCallFragment
import com.identify.sdk.webrtc.wait.CallWaitingFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import permissions.dispatcher.*


@RuntimePermissions
class IdentifyActivity : AppCompatActivity(),
    NoInternetDialogFragment.NoInternetClickInterface,
    OnFragmentTransactionListener {

    private var noInternetAlertDialogFragment : NoInternetDialogFragment?= null

    private var customerInformationEntity : CustomerInformationEntity ?= null



    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)
        changeStatusBarColor()
        showCameraWithPermissionCheck()
        supportActionBar?.hide()
        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer { isConnected ->
            isConnected?.let {
               isNetworkAvailable(it)
            }
        })
    }



    @ExperimentalCoroutinesApi
    private fun showCallWaitingFragment(customer : CustomerInformationEntity){
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallWaitingFragment.newInstance(customer),CallWaitingFragment::class.java.toString()).commitAllowingStateLoss()
    }

    @ExperimentalCoroutinesApi
    private fun showCallingFragment(){
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallingFragment.newInstance(),CallingFragment::class.java.toString()).commitAllowingStateLoss()
    }

    @ExperimentalCoroutinesApi
    private fun removeCallingFragment(){
        supportFragmentManager.findFragmentByTag(CallingFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    @ExperimentalCoroutinesApi
    private fun removeStartedCallFragment() {
        supportFragmentManager.findFragmentByTag(StartedCallFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    @ExperimentalCoroutinesApi
    private fun showCallStartedFragment() {
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,StartedCallFragment.newInstance(),StartedCallFragment::class.java.toString()).commitAllowingStateLoss()
    }


    fun changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this,R.color.primaryDarkColor)
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @ExperimentalCoroutinesApi
    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun showCamera() {
        intent.getParcelableExtra<CustomerInformationEntity>("customer")?.let {
            customerInformationEntity = it
            showCallWaitingFragment(it)
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun showRationaleForCamera(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_camera_rationale, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun onCameraNeverAskAgain() {
        openPermissionSettings()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun onPermissionDenied(){
        finish()
    }

    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }


    @ExperimentalCoroutinesApi
    override fun onStop() {
        super.onStop()
        removeCallingFragment()
        removeStartedCallFragment()
    }





    private fun isNetworkAvailable(isOnline: Boolean) {
         when(isOnline){
             true->{
                 clearNetworkDialog()
             }
             false->{
                 if (noInternetAlertDialogFragment == null){
                     noInternetAlertDialogFragment = NoInternetDialogFragment.newInstance()
                     supportFragmentManager.beginTransaction().add(noInternetAlertDialogFragment!!,NoInternetDialogFragment::class.java.toString()).commitAllowingStateLoss()
                     noInternetAlertDialogFragment?.isCancelable = true
                 }
             }
         }
    }


    override fun noInternetClickListener() {
        clearNetworkDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearNetworkDialog()
    }

    fun clearNetworkDialog(){
        noInternetAlertDialogFragment?.let {
            it.dismissAllowingStateLoss()
            noInternetAlertDialogFragment = null
        }
    }

    @ExperimentalCoroutinesApi
    override fun onOpenWaitFragment() {
        customerInformationEntity?.let {
            showCallWaitingFragment(it)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onOpenCallingFragment() {
        showCallingFragment()
    }

    override fun onOpenStartedCallFragment() {
        showCallStartedFragment()
    }

    @ExperimentalCoroutinesApi
    override fun onRemoveCallingFragment() {
        removeCallingFragment()
    }

    @ExperimentalCoroutinesApi
    override fun onRemoveStartedCallFragment() {
        removeStartedCallFragment()
    }




}