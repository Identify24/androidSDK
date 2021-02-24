package com.identify.sdk

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.identify.sdk.SdkApp.destroy
import com.identify.sdk.SdkApp.identityOptions
import com.identify.sdk.face.FaceDetectionFragment
import com.identify.sdk.form.WebViewFormFragment
import com.identify.sdk.form.WebViewFormListener
import com.identify.sdk.intro.IntroFragment
import com.identify.sdk.intro.IntroListener
import com.identify.sdk.mrz.NfcFragment
import com.identify.sdk.mrz.OcrFragment
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.SocketActionType
import com.identify.sdk.repository.model.dto.MrzDto
import com.identify.sdk.repository.model.enums.*
import com.identify.sdk.unavaible_internet.livedata.ConnectionLiveData
import com.identify.sdk.unavaible_internet.ui.NoInternetDialogFragment
import com.identify.sdk.util.alert
import com.identify.sdk.webrtc.CallViewModel
import com.identify.sdk.webrtc.OnFragmentTransactionListener
import com.identify.sdk.webrtc.calling.CallingFragment
import com.identify.sdk.webrtc.started.StartedCallFragment
import com.identify.sdk.webrtc.thanks.ThankYouFragment
import com.identify.sdk.webrtc.wait.CallWaitingFragment
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jmrtd.lds.icao.MRZInfo
import permissions.dispatcher.*
import java.util.*


@RuntimePermissions
@ExperimentalCoroutinesApi
class IdentifyActivity : AppCompatActivity(),
    NoInternetDialogFragment.NoInternetClickInterface,
    OnFragmentTransactionListener, WebViewFormListener,IdentifyResultListener,IntroListener {

    private var noInternetAlertDialogFragment : NoInternetDialogFragment?= null

    private var adapter : NfcAdapter ?= null

    var viewModel : CallViewModel ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // close screen saver
        viewModel = ViewModelProvider(this).get(CallViewModel::class.java)
        intent.getStringExtra("language").let {
            setLocale(it,baseContext)
        }
        setContentView(R.layout.activity_identify)
        changeStatusBarColor()
        supportActionBar?.hide()
        observeDataChanges()
        sdkListenerSetup()

        identityOptions?.getOpenIntroPage()?.let {
            if (it) showIntroFragment()
            else onCheckPermission()

        }

    }

    private fun sdkListenerSetup() {
        IdentifySdk.getInstance().sdkCloseListener = object : SdkCloseListener{
            override fun finishSdk() {
                this@IdentifyActivity.finish()
            }

        }

    }


    private fun setLocale(selectedLocale: String?,context: Context) {
        var locale : Locale?= null
        if (selectedLocale == "en" || selectedLocale == "tr" || selectedLocale == "de"){
            locale = Locale(selectedLocale)
        }else {
            locale = Locale("en")
        }

        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )


    }

    private fun showThankYouFragment(){
        if (supportFragmentManager.findFragmentByTag(ThankYouFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, ThankYouFragment.newInstance(),ThankYouFragment::class.java.toString()).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "show ThankYouFragment")
        }
    }

    private fun removeThankYouFragment(){
        supportFragmentManager.findFragmentByTag(ThankYouFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove ThankYouFragment")
        }
    }


    private fun showFaceDetectionFragment(){
        if (supportFragmentManager.findFragmentByTag(FaceDetectionFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, FaceDetectionFragment.newInstance(),FaceDetectionFragment::class.java.toString()).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "show FaceDetectionFragment")
        }
    }

    private fun removeFaceDetectionFragment(){
        supportFragmentManager.findFragmentByTag(FaceDetectionFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove FaceDetectionFragment")
        }
    }

    private fun showOcrFragment(){
        if (supportFragmentManager.findFragmentByTag(OcrFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, OcrFragment.newInstance(),OcrFragment::class.java.toString()).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "show OcrFragment")
        }
    }

    private fun removeOcrFragment(){
        supportFragmentManager.findFragmentByTag(OcrFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove OcrFragment")
        }
    }

    private fun showWebViewFormFragment(customer : CustomerInformationEntity){
        if (supportFragmentManager.findFragmentByTag(WebViewFormFragment::class.java.toString()) == null ){
            customer.webviewUrl?.let { url ->
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,WebViewFormFragment.newInstance(url),WebViewFormFragment::class.java.toString()).commitAllowingStateLoss()
                println("şimdi buradaydı = " + "show WebViewFormFragment")
            }
        }
    }

    private fun removeWebViewFormFragment(){
        supportFragmentManager.findFragmentByTag(WebViewFormFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove WebViewFormFragment")
        }
    }


    private fun showCallWaitingFragment(){
        viewModel?.customerInformationEntity?.let {
            if (supportFragmentManager.findFragmentByTag(CallWaitingFragment::class.java.toString()) == null ){
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallWaitingFragment.newInstance(it),CallWaitingFragment::class.java.toString()).commitAllowingStateLoss()
                println("şimdi buradaydı = " + "show CallWaitingFragment")
            }
        }
    }

    private fun showIntroFragment(){
            if (supportFragmentManager.findFragmentByTag(IntroFragment::class.java.toString()) == null ){
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,IntroFragment.newInstance(),IntroFragment::class.java.toString()).commitAllowingStateLoss()
                println("şimdi buradaydı = " + "show IntroFragment")
            }
    }

    private fun removeIntroFragment(){
        supportFragmentManager.findFragmentByTag(IntroFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove IntroFragment")
        }
    }

    private fun removeCallWaitingFragment(){
        supportFragmentManager.findFragmentByTag(CallWaitingFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + "remove CallWaitingFragment")
        }
    }


    private fun showCallingFragment(){
        if (supportFragmentManager.findFragmentByTag(CallingFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,CallingFragment.newInstance(),CallingFragment::class.java.toString()).commitAllowingStateLoss()
            println("şimdi buradaydı = " + " show CallingFragment")
        }
    }


    private fun removeCallingFragment(){
        supportFragmentManager.findFragmentByTag(CallingFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + " remove CallingFragment")
        }
    }


    private fun removeStartedCallFragment() {
        supportFragmentManager.findFragmentByTag(StartedCallFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + " remove StartedCallFragment")
        }
    }


    private fun showCallStartedFragment() {
        if (supportFragmentManager.findFragmentByTag(StartedCallFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,StartedCallFragment.newInstance(),StartedCallFragment::class.java.toString()).commitAllowingStateLoss()
            println("şimdi buradaydı = " + " show CallStartedFragment")
        }
    }


    private fun showNfcFragment(mrzInfo: MRZInfo, customer : CustomerInformationEntity) {
        if (supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString()) == null ){
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,NfcFragment.newInstance(mrzInfo,customer),NfcFragment::class.java.toString()).commitAllowingStateLoss()
            startNfcReader()
            println("şimdi buradaydı = " + " show NfcFragment")
        }

    }

    private fun removeNfcFragment() {
        supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())?.let {
            supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            println("şimdi buradaydı = " + " remove NfcFragment")
        }
        clearNfcReader()
    }


    private fun changeStatusBarColor(){
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         window.apply {
             clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
             addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
             decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
             statusBarColor = Color.TRANSPARENT
         }
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
            viewModel?.customerInformationEntity = it
                when(it.status){
                    StatusType.GO_WEB_VIEW.type->{
                        showWebViewFormFragment(it)
                    }
                    StatusType.GO_MRZ.type->{
                        when(identityOptions?.getIdentityType()){
                            IdentityType.ONLY_CALL->{
                                showCallWaitingFragment()
                            }
                            IdentityType.WITHOUT_CALL->{
                                checkNfcExisting()
                            }
                            IdentityType.FULL_PROCESS->{
                                checkNfcExisting()
                            }
                        }
                        removeIntroFragment()
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


    fun observeDataChanges(){
        viewModel?.activitySocketResponse?.observe(this, Observer {
            when(it.action){
                    SocketActionType.MESSAGE_SENDED.type->{
                        when(viewModel?.nfcStatusType){
                            NfcStatusType.AVAILABLE->{
                                showOcrFragment()
                            }
                            NfcStatusType.NOT_AVAILABLE->{
                                showCallWaitingFragment()
                            }
                            else -> {
                                 showCallWaitingFragment()
                            }
                        }
                    }
                }

        })

        viewModel?.activityFailure?.observe(this, Observer {
            if (it){
                Toasty.error(this,getString(R.string.reason_socket_connection),Toasty.LENGTH_SHORT).show()
            }
        })


        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer { isConnected ->
            isConnected?.let {
                isNetworkAvailable(it)
            }
        })
    }



    private fun clearNfcReader(){
        adapter?.let {
            it.disableForegroundDispatch(this)
            adapter = null
        }
    }


    private fun checkNfcExisting(){
        setNfcAdapter()
        if (adapter == null){
            viewModel?.nfcStatusType = NfcStatusType.NOT_AVAILABLE
            viewModel?.connectSocket(true,NfcStatusType.NOT_AVAILABLE)
        }  else{
            viewModel?.nfcStatusType = NfcStatusType.AVAILABLE
            viewModel?.connectSocket(true,NfcStatusType.AVAILABLE)
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

    override fun onPause() {
        super.onPause()
        IdentifySdk.getInstance().setSdkLifeCycle(SdkLifeCycleType.PAUSED)
        clearNfcReader()
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.findFragmentByTag(NfcFragment::class.java.toString())?.let { fragment ->
            if (fragment.isVisible) {
                startNfcReader()
            }
        }
        IdentifySdk.getInstance().setSdkLifeCycle(SdkLifeCycleType.RESUMED)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearNetworkDialog()
        clearNfcReader()
        destroy()
        viewModel?.disconnectSocket()
        IdentifySdk.getInstance().setSdkLifeCycle(SdkLifeCycleType.DESTROYED)
        IdentifySdk.getInstance().sdkDestroyed()
    }

    fun clearNetworkDialog(){
        noInternetAlertDialogFragment?.let {
            it.dismissAllowingStateLoss()
            noInternetAlertDialogFragment = null
        }
    }


    override fun onOpenWaitFragment() {
            showCallWaitingFragment()
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
        viewModel?.customerInformationEntity?.let { showNfcFragment(mrzInfo, it) }
    }

    override fun onRemoveNfcFragment() {
        removeNfcFragment()
    }

    override fun onOpenOcrFragment() {
        showOcrFragment()
    }

    override fun onRemoveOcrFragment() {
        removeOcrFragment()
    }

    override fun onRemoveFaceDetectionFragment() {
        removeFaceDetectionFragment()
    }

    override fun onOpenFaceDetectionFragment() {
       showFaceDetectionFragment()
    }

    override fun onOpenThankYouFragment() {
        showThankYouFragment()
    }

    override fun onRemoveThankYouFragment() {
       removeThankYouFragment()
    }

    override fun onRemoveWaitFragment() {
        removeCallWaitingFragment()
    }

    override fun onSuccess() {
        removeWebViewFormFragment()
        showCallWaitingFragment()
    }

    override fun onFailure() {

    }

    override fun nfcProcessFinished(isSuccess : Boolean, mrzDto: MrzDto?) {
        IdentifySdk.getInstance().resultCallback(Pair(IdentityResultType.NFC,mrzDto))
    }

    override fun vitalityDetectionProcessFinished() {
        IdentifySdk.getInstance().resultCallback(Pair(IdentityResultType.FACE,null))
    }

    override fun callProcessFinished() {
        IdentifySdk.getInstance().resultCallback(Pair(IdentityResultType.CALL,null))
    }


    override fun onCheckPermission() {
        showCameraWithPermissionCheck()
    }


}