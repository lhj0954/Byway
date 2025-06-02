package com.example.byway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;

public class MypageActivity extends AppCompatActivity {

    private TextView nameText;
    private TextView logoutBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        nameText = findViewById(R.id.user_name_text);
        logoutBtn = findViewById(R.id.text_logout); // Mypage 로그아웃 버튼 id

//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            // 로그인 되어있지 않다면 MainActivity로 강제 이동
//            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//            return;
//        }

        fetchAndDisplayUserName();

        logoutBtn.setOnClickListener(v -> logoutAll(MypageActivity.this));
    }

    private void fetchAndDisplayUserName() {
        String uid = UserManager.getUid(this);
        if (uid == null) {
            nameText.setText("⚙ 로그인 정보 없음");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null) {
                            nameText.setText("⚙ " + name + " 님");
                        } else {
                            nameText.setText("⚙ 이름 정보 없음");
                        }
                    } else {
                        nameText.setText("⚙ 사용자 정보 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                    nameText.setText("⚙ 오류 발생");
                });
    }


    private void logoutAll(Activity activity) {
        // Firebase 로그아웃
        FirebaseAuth.getInstance().signOut();

        // Google 로그아웃 - 로그인 시와 동일한 옵션 사용
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d("Logout", "Google 로그아웃 완료");

            // 로그인 상태 초기화
            PreferenceManager.clear(activity);

            // Kakao 로그아웃
            UserApiClient.getInstance().logout(error -> {
                if (error != null) {
                    Log.e("Logout", "Kakao 로그아웃 실패", error);
                } else {
                    Log.d("Logout", "Kakao 로그아웃 성공");
                }

                Toast.makeText(activity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();



                // SplashActivity로 이동
                Intent intent = new Intent(activity, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();

                return null;
            });
        });
    }


}
