package com.example.byway;

import android.app.Application;
import android.util.Log;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key));
        Log.d("KakaoInitCheck", KakaoSdk.INSTANCE.getApplicationContextInfo() != null ? "SDK 초기화됨" : "SDK 초기화 안됨");


    }
}
