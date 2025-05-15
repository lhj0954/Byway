package com.example.byway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.byway.R;
import com.example.byway.MypageActivity;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button googleBtn = findViewById(R.id.btn_google);
        Button kakaoBtn = findViewById(R.id.btn_kakao);

        googleBtn.setOnClickListener(v -> goToMypage());
        kakaoBtn.setOnClickListener(v -> goToMypage());
    }

    private void goToMypage() {
        Intent intent = new Intent(this, MypageActivity.class);
        startActivity(intent);
    }
}
