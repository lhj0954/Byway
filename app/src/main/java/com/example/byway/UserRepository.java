package com.example.byway;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    public interface UserNameCallback {
        void onSuccess(String name);
        void onFailure(String error);
    }

    public static void getCurrentUserName(UserNameCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure("로그인된 사용자가 없습니다.");
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null) {
                            callback.onSuccess(name);
                        } else {
                            callback.onFailure("사용자 이름이 없습니다.");
                        }
                    } else {
                        callback.onFailure("사용자 문서를 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Firestore 접근 실패", e);
                    callback.onFailure("Firestore 오류: " + e.getMessage());
                });
    }
}
