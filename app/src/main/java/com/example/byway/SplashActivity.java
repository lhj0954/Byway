package com.example.byway;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (FirebaseAuth.getInstance().getCurrentUser() == null && PreferenceManager.isKakaoLoggedIn(this)) {
                PreferenceManager.clear(this);
            }
            startActivity(new Intent(SplashActivity.this, MainActivity.class));

//            if (user != null) {
//                // 🔐 로그인 되어 있으면 Main으로
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            } else {
//                // 🔓 로그인 안 되어 있으면 Login으로
//                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//            }

            finish(); // SplashActivity 종료
        }, 2000); // 2초 지연
    }
}
