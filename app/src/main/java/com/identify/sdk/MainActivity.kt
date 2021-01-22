package com.identify.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        IdentifySdk.getInstance().startIdentification(this,"276ddff8-280b-11eb-a693-005056bb3f3f","tr")
        IdentifySdk.getInstance().identifyErrorListener = object : IdentifyErrorListener{
            override fun identError(errorMessage: String) {
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
            }

        }
    }




}