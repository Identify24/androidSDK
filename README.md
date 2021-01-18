# Gradle
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
	
 <pre>allprojects { 
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}</pre>
	
Step 2. Add the dependency

<pre>implementation 'com.github.Identify24:androidSDK:2.0.0'</pre>

# Usage

Get an singleton object from identify sdk. Then connect to sdk with ID number, thats all.

# For Kotlin
<pre>
   IdentifySdk.getInstance().startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx")
        IdentifySdk.getInstance().identifyErrorListener = object : IdentifyErrorListener{
            override fun identError(errorMessage: String) {
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
            }

        }
</pre>

# For Java

<pre>
  IdentifySdk.getInstance().startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx");
        IdentifySdk.getInstance().setIdentifyErrorListener(new IdentifyErrorListener() {
            @Override
            public void identError(@NotNull String errorMessage) {
                Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
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
