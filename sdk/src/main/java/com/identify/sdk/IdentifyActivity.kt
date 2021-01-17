package com.identify.sdk

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.evren.common.dialog.WebViewFormListener
import com.identify.sdk.form.WebViewFormFragment
import com.identify.sdk.mrz.NfcFragment
import com.identify.sdk.mrz.OcrFragment
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.enums.StatusType
import com.identify.sdk.unavaible_internet.livedata.ConnectionLiveData
import com.identify.sdk.unavaible_internet.ui.NoInternetDialogFragment
import com.identify.sdk.util.alert
import com.identify.sdk.util.positiveButton
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.calling.CallingFragment
import com.identify.sdk.webrtc.started.StartedCallFragment
import com.identify.sdk.webrtc.wait.CallWaitingFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jmrtd.lds.icao.MRZInfo
import permissions.dispatcher.*


@RuntimePermissions
@ExperimentalCoroutinesApi
class IdentifyActivity : AppCompatActivity(),
    NoInternetDialogFragment.NoInternetClickInterface,
    OnFragmentTransactionListener, WebViewFormListener {

    private var noInternetAlertDialogFragment : NoInternetDialogFragment?= null

    private var customerInformationEntity : CustomerInformationEntity ?= null

    private var adapter : NfcAdapter ?= null


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



    private fun showOcrFragment(){
     supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, OcrFragment.newInstance(),OcrFragment::class.java.toString()).commitAllowingStateLoss()
    }

    private fun removeOcrFragment(){
        supportFragmentManager.findFragmentByTag(OcrFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    private fun showWebViewFormFragment(customer : CustomerInformationEntity){
        customer.webviewUrl?.let { url ->
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,WebViewFormFragment.newInstance(url),WebViewFormFragment::class.java.toString()).commitAllowingStateLoss()
        }
    }

    private fun removeWebViewFormFragment(){
        supportFragmentManager.findFragmentByTag(WebViewFormFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }


    private fun showCallWaitingFragment(customer : CustomerInformationEntity){
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallWaitingFragment.newInstance(customer),CallWaitingFragment::class.java.toString()).commitAllowingStateLoss()
    }


    private fun showCallingFragment(){
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallingFragment.newInstance(),CallingFragment::class.java.toString()).commitAllowingStateLoss()
    }


    private fun removeCallingFragment(){
        supportFragmentManager.findFragmentByTag(CallingFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }


    private fun removeStartedCallFragment() {
        supportFragmentManager.findFragmentByTag(StartedCallFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }


    private fun showCallStartedFragment() {
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,StartedCallFragment.newInstance(),StartedCallFragment::class.java.toString()).commitAllowingStateLoss()
    }

    private fun showNfcFragment(mrzInfo: MRZInfo, customer : CustomerInformationEntity) {
        val fragment : Fragment? = supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())
        if (fragment == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,NfcFragment.newInstance(mrzInfo,customer),NfcFragment::class.java.toString()).commitAllowingStateLoss()
            startNfcReader()
        }

    }

    private fun removeNfcFragment() {
        supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        clearNfcReader()
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


    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun showCamera() {
        intent.getParcelableExtra<CustomerInformationEntity>("customer")?.let {
            customerInformationEntity = it
                when(it.status){
                    StatusType.GO_CALL.type->{
                        showCallWaitingFragment(it)
                    }
                    StatusType.GO_MRZ.type->{
                        checkNfcExisting({
                            showOcrFragment()
                        },{
                            showWebViewFormFragment(it)
                        })
                    }
                    else->{
                        showWebViewFormFragment(it)
                    }
                }

        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tag =
                intent.extras?.getParcelable<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.techList?.let {
                if (it.contains("android.nfc.tech.IsoDep")) {
                    supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())?.let { fragment ->
                        if (fragment.isVisible){
                            val isoDep = IsoDep.get(tag)
                            isoDep.timeout = 5000
                            (fragment as NfcFragment).triggerNfcReader(isoDep)
                            fragment.finishedNfcProcess = {
                                clearNfcReader()
                                startNfcReader()
                            }
                        }
                    }

                }}}

    }

    private fun setNfcAdapter(){
        if ( adapter == null ) adapter = NfcAdapter.getDefaultAdapter(this)
    }

    fun startNfcReader(){
        setNfcAdapter()
        val intent = Intent(this, this.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val filter =
            arrayOf(arrayOf("android.nfc.tech.IsoDep"))
        adapter?.enableForegroundDispatch(this, pendingIntent, null, filter)
        checkNfcEnabled()
    }

    private fun checkNfcEnabled() {
        if (adapter?.isEnabled != true){
            showNfcDialog()
            return
        }
    }

    private fun showNfcDialog(){
        alert(false, getString(R.string.go_to_setting),null,getString(R.string.nfc_off),getString(R.string.nfc_off_desc),{ dialog ->
            startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
            dialog.dismiss()
        },{},{})

    }



    private fun clearNfcReader(){
        adapter?.let {
            it.disableForegroundDispatch(this)
            adapter = null
        }
    }

    private fun checkNfcExisting(itsOnDevice : () -> Unit, notOnDevice : () -> Unit ){
        setNfcAdapter()
        if (adapter == null) notOnDevice() else itsOnDevice()
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


    override fun onStop() {
        super.onStop()
        removeCallingFragment()
        removeStartedCallFragment()
    }


    override fun onPause() {
        super.onPause()
        clearNfcReader()
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())?.let { fragment ->
            if (fragment.isVisible) {
                startNfcReader()
            }
        }
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


    override fun onOpenWaitFragment() {
        customerInformationEntity?.let {
            showCallWaitingFragment(it)
        }
    }


    override fun onOpenCallingFragment() {
        showCallingFragment()
    }

    override fun onOpenStartedCallFragment() {
        showCallStartedFragment()
    }


    override fun onRemoveCallingFragment() {
        removeCallingFragment()
    }


    override fun onRemoveStartedCallFragment() {
        removeStartedCallFragment()
    }

    override fun onOpenNfcFragment(mrzInfo: MRZInfo) {
        customerInformationEntity?.let { showNfcFragment(mrzInfo, it) }
    }

    override fun onRemoveNfcFragment() {
        removeNfcFragment()
    }

    override fun onRemoveOcrFragment() {
        removeOcrFragment()
    }

    override fun onSuccess() {
        removeWebViewFormFragment()
        customerInformationEntity?.let { showCallWaitingFragment(it) }
    }

    override fun onFailure() {

    }


}