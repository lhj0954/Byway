package com.example.byway;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class UserManager {
    public static String getUid(Context context) {
        String firebaseUid = FirebaseAuth.getInstance().getUid();
        if (firebaseUid != null) return firebaseUid;

        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return prefs.getString("uid", null);
    }

    public static void saveUid(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        prefs.edit().putString("uid", uid).apply();
    }
}
