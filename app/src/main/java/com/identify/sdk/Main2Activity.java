package com.identify.sdk;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.identify.sdk.repository.model.dto.MrzDto;
import com.identify.sdk.repository.model.enums.IdentityType;

import org.jetbrains.annotations.NotNull;

public class Main2Activity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        IdentityOptions options = new IdentityOptions.Builder()
                .setIdentityType(IdentityType.ONLY_CALL) // Default value IdentityType.FULL_PROCESS
                .setNfcExceptionCount(5) // Default value 3
                .build();


        IdentifySdk identifyObject = new IdentifySdk.Builder()
                .api("api url")
                .socket("socket url","socket port")
                .stun("stun url","stun port")
                .turn("turn url","turn port","turn username","turn password")
                .options(options)
                .build();

        identifyObject.startIdentification(this,"xxxx-xxxx-xxxx-xxxx-xxxxxxx","language");

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
    }
}
