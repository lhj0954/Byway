package com.example.byway;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MypageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        String name = getIntent().getStringExtra("name");
        TextView nameText = findViewById(R.id.user_name_text);

        if (name != null) {
            nameText.setText("⚙ " + name + " 님");
        } else {
            nameText.setText("⚙ 사용자 정보 없음");
        }
    }
}
