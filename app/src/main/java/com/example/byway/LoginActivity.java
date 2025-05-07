package com.example.byway;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;
    private static final String SERVER_CLIENT_ID = "YOUR_SERVER_CLIENT_ID_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        oneTapClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(SERVER_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                ).build();

        Button btnKakao = findViewById(R.id.btn_kakao_login);
        Button btnGoogle = findViewById(R.id.btn_google_login);

        btnKakao.setOnClickListener(view -> kakaoLogin());
        btnGoogle.setOnClickListener(view -> googleLogin());
    }

    private void googleLogin() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                null, 0, 0, 0, null
                        );
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("Google", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                    }
                })
                .addOnFailureListener(this, e -> Log.d("Google", "One Tap Failed: " + e.getLocalizedMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();

                if (idToken != null) {
                    showSuccessMessage("Google 로그인 성공: " + idToken);
                } else {
                    showErrorMessage("Google 로그인 실패: No ID Token");
                }

            } catch (ApiException e) {
                showErrorMessage("Google 로그인 실패: " + e.getStatusCode());
            }
        }
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

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("LoginActivity", message);
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i("LoginActivity", message);
    }
}
