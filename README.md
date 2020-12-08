# How to
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

<pre>implementation 'com.github.Identify24:androidSDK:1.0.0'</pre>

