package com.example.byway;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> signInLauncher;

    // 카카오 로그인
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // 카카오 유저정보 db에 저장
    private void saveUserToFirestore(String uid, String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);


        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "카카오 사용자 정보 저장 성공");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "카카오 사용자 정보 저장 실패", e);
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KakaoSdk.init(this, "c652e0a453d542758e0c379495dfc05b");

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            for (Signature signature : info.signingInfo.getApkContentsSigners()) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash", "KeyHash: " + keyHash);
            }
        } catch (Exception e) {
            Log.e("KeyHash", "Error", e);
        }



//        // ✅ 이미 로그인된 상태라면 바로 메인으로 이동
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//            return;
//        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        Button kakaoBtn = findViewById(R.id.btn_kakao);

        kakaoBtn.setOnClickListener(v -> {
            UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
                if (error != null) {
                    Log.e("KakaoLogin", "카카오 로그인 실패", error);
                    Toast.makeText(this, "카카오 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (token != null) {
                    Log.d("KakaoLogin", "카카오 로그인 성공: " + token.getAccessToken());

                    UserApiClient.getInstance().me((user, error2) -> {
                        if (error2 != null) {
                            Log.e("KakaoUserInfo", "사용자 정보 요청 실패", error2);
                        } else if (user != null) {
                            String uid = user.getId().toString();
                            String name = "Unknown";

                            if (user.getKakaoAccount().getProfile() != null) {
                                name = user.getKakaoAccount().getProfile().getNickname();
                            }

                            UserManager.saveUid(LoginActivity.this, uid);
                            PreferenceManager.setKakaoLoggedIn(LoginActivity.this, true);
                            saveUserToFirestore(uid, name);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.e("KakaoUserInfo", "user or kakaoAccount is null");
                        }
                        return null;
                    });

                    //goToMain();
                }
                return null;
            });
        });


        // 🔐 Google 로그인 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // ✅ 최신 Activity Result 방식 등록
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("GoogleLogin", "signInLauncher 콜백 진입");

                    Log.d("GoogleLogin", "result.getResultCode(): " + result.getResultCode());
                    Log.d("GoogleLogin", "result.getData(): " + result.getData());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        Log.d("GoogleLogin", "GoogleSignIn 성공, ID Token: ");
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d("GoogleLogin", "GoogleSignIn 성공, ID Token: " + account.getIdToken());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.e("GoogleLogin", "GoogleSignIn 실패", e);
                            Toast.makeText(this, "Google 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // 🔘 로그인 버튼 클릭 시
        Button googleBtn = findViewById(R.id.btn_google);
        googleBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });

        /* findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        }); */



    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("GoogleLogin", "idToken: " + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = null;
                        if (user != null) {
                            String uid = user.getUid();
                            UserManager.saveUid(LoginActivity.this, uid);

                            name = user.getDisplayName();
                            String email = user.getEmail();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);

                            db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "사용자 정보 저장 성공");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "사용자 정보 저장 실패", e);
                                    });
                        }

                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();


                        // ✅ MainActivity로 이름 전달!
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", name); // 이름 전달
                        startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
