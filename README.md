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

<pre>implementation 'com.github.Identify24:androidSDK:2.1.0'</pre>

# Usage

First, get a singleton object by set the environment variables from identify sdk. Then connect to sdk with ID number, thats all.

# For Kotlin
<pre>
  val identifyObject = IdentifySdk.Builder()
	.api("api url")
        .socket("socket url","socket port")
        .stun("stun url","stun port")
        .turn("turn url","turn port","turn username","turn password")
        .build()
 identifyObject.startIdentification(this,"276ddff8-280b-11eb-a693-005056bb3f3f","tr")
 identifyObject.identifyErrorListener = object : IdentifyErrorListener{
    override fun identError(errorMessage: String) {
        Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
    }
}
</pre>

# For Java



<pre>

 IdentifySdk identifyObject = new IdentifySdk.Builder()
         .api("api url")
         .socket("socket url","socket port")
         .stun("stun url","stun port")
         .turn("turn url","turn port","turn username","turn password")
         .build();
 identifyObject.startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx","language");
 identifyObject.setIdentifyErrorListener(new IdentifyErrorListener() {
    @Override
    public void identError(@NotNull String errorMessage) {
        Toast.makeText(getBaseContext(),errorMessage,Toast.LENGTH_SHORT).show();
    }
});

</pre>

Language is optional(can be null). Default value is English. Supported languages are English, German and Turkish.
* for Turkish language parameter -> tr
* for English language parameter -> en
* for German language parameter -> de

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
