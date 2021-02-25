# Gradle
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

!!! MinSdk version must be at least 21
	
 <pre>allprojects { 
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}</pre>
	
Step 2. Add the dependency

<pre>implementation 'com.github.Identify24:androidSDK:2.3.2'</pre>

# Usage

Firstly, you have to create options for sdk.Then get a singleton object by set the environment variables and options from identify sdk. Then connect to sdk with ID number, thats all.

# For Kotlin
<pre>
   val options = IdentityOptions.Builder()
            .setIdentityType(IdentityType.ONLY_CALL) // Default value IdentityType.FULL_PROCESS
            .setNfcExceptionCount(5) // Default value 3
	    .setOpenIntroPage(false) // Default true
	    .setCallConnectionTimeOut(5000) //Default 10000 ms
            .build()

  val identifyObject = IdentifySdk.Builder()
	.api("api url")
        .socket("socket url","socket port")
        .stun("stun url","stun port")
        .turn("turn url","turn port","turn username","turn password")
	.options(options)
        .build()
	
 identifyObject.startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx","tr")
 
</pre>

# For Java



<pre>

  IdentityOptions options = new IdentityOptions.Builder()
                .setIdentityType(IdentityType.ONLY_CALL) // Default value IdentityType.FULL_PROCESS
                .setNfcExceptionCount(5) // Default value 3
		.setOpenIntroPage(false) // Default true
		.setCallConnectionTimeOut(5000) //Default 10000 ms
                .build();

 IdentifySdk identifyObject = new IdentifySdk.Builder()
         .api("api url")
         .socket("socket url","socket port")
         .stun("stun url","stun port")
         .turn("turn url","turn port","turn username","turn password")
	 .options(options)
         .build();
	 
 identifyObject.startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx","language");
 

</pre>

Language is optional(can be null). Default value is English. Supported languages are English, German and Turkish.
* for Turkish language parameter -> tr
* for English language parameter -> en
* for German language parameter -> de

NfcExceptionCount -> This number specifies the maximum number of errors to be received when reading nfc.This process is passed after the number of errors exceeds this number. This is optional(can be null, default value = 3)

Sdk processes work like this 
ocr -> nfc -> vitality detection -> call

IdentityType -> where will the process begin and end.There are 3 options.
* FULL_PROCESS -> all process working
* ONLY_CALL -> without nfc-ocr and vitality detection process working
* WITHOUT_CALL -> without call process working


# Listeners

* IdentifyErrorListener -> "xxxx-xxxx-xxxx-xxxx-xxxxxxx" If the ident id is not found
* IdentifyResultListener -> ocr -> nfc -> vitality detection -> call = when processes are over
* SdkLifeCycleListener -> get sdk lifecycle 


# For Kotlin

<pre>

       identifyObject.identifyErrorListener = object : IdentifyErrorListener{
            override fun identError(errorMessage: String) {
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
            }
        }
        identifyObject.identifyResultListener = object : IdentifyResultListener{
            override fun nfcProcessFinished(isSuccess: Boolean, mrzDto: MrzDto?) {
                Toast.makeText(this@MainActivity,"nfc process finished",Toast.LENGTH_SHORT).show()
            }

            override fun vitalityDetectionProcessFinished() {
               Toast.makeText(this@MainActivity,"face process finished",Toast.LENGTH_SHORT).show()
            }
            override fun callProcessFinished() {
                Toast.makeText(this@MainActivity,"call process finished",Toast.LENGTH_SHORT).show()
                identifyObject.closeSdk() // You can finish sdk with this method when in the process you want
            }

        }

        identifyObject.sdkLifeCycleListener = object : SdkLifeCycleListener {
            override fun sdkDestroyed() {
                // Sdk Activity Destroyed
            }

            override fun sdkPaused() {
                // Sdk Activity Paused
            }

            override fun sdkResumed() {
                // Sdk Activity Resumed
            }

        }
	
	
</pre>

# For Java

<pre>

        identifyObject.setIdentifyErrorListener(new IdentifyErrorListener() {
            @Override
            public void identError(@NotNull String errorMessage) {
                Toast.makeText(getBaseContext(),errorMessage,Toast.LENGTH_SHORT).show();
            }
        });

        identifyObject.setIdentifyResultListener(new IdentifyResultListener() {
            @Override
            public void nfcProcessFinished(boolean isSuccess, @org.jetbrains.annotations.Nullable MrzDto mrzDto) {
                Toast.makeText(getBaseContext(),"nfc process finished",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void vitalityDetectionProcessFinished() {
                Toast.makeText(getBaseContext(),"face process finished",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void callProcessFinished() {
                Toast.makeText(getBaseContext(),"call process finished",Toast.LENGTH_SHORT).show();
                identifyObject.closeSdk(); // You can finish sdk with this method when in the process you want
            }
        });

        identifyObject.setSdkLifeCycleListener(new SdkLifeCycleListener() {
            @Override
            public void sdkDestroyed() {
                // Sdk Activity Destroyed
            }

            @Override
            public void sdkPaused() {
                // Sdk Activity Paused
            }

            @Override
            public void sdkResumed() {
                // Sdk Activity Resumed
            }
        });

</pre>


# Permission

Permissions used within the sdk (don't need to add, attached in the library)

```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE"/>
    <uses-feature android:name="android.hardware.nfc" />
    <uses-feature android:name="android.hardware.camera" />
```


# LICENSE

Identify Android Sdk Copyright (C) 2020  Identify Turkey

[GNU General Public License v3.0](https://github.com/Identify24/androidSDK/blob/main/LICENSE.md)

