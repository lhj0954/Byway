package com.example.byway;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Google Sign-In Initialization
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnKakao = findViewById(R.id.btn_kakao_login);
        Button btnGoogle = findViewById(R.id.btn_google_login);

        btnKakao.setOnClickListener(view -> kakaoLogin());
        btnGoogle.setOnClickListener(view -> googleLogin());
    }

    private void kakaoLogin() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
            if (error != null) {
                showErrorMessage("카카오 로그인 실패: " + error.getMessage());
            } else if (token != null) {
                showSuccessMessage("카카오 로그인 성공");
            }
            return null;
        });
    }

    private void googleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showSuccessMessage("구글 로그인 성공: " + account.getEmail());
        } catch (ApiException e) {
            showErrorMessage("구글 로그인 실패: " + e.getStatusCode());
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("LoginActivity", message);
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i("LoginActivity", message);
    }
}
