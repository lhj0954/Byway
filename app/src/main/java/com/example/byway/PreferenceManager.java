//package com.example.byway;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//import com.google.firebase.auth.FirebaseAuth;
//
//public class PreferenceManager {
//
//    private static final String PREF_NAME = "UserPrefs";
//    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
//
//    public static void setLoggedIn(Context context, boolean isLoggedIn) {
//        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
//        editor.apply();
//    }
//
//    public static boolean isLoggedIn(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
//    }
//
//    public static void clear(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.clear();
//        editor.apply();
//    }
//
//    // ✅ 추가: FirebaseAuth 기준 로그인 상태 확인
//    public static boolean isFirebaseLoggedIn() {
//        return FirebaseAuth.getInstance().getCurrentUser() != null;
//    }
//
//    // ✅ 추가: 현재 로그인된 사용자 UID 반환
//    public static String getUid() {
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            return FirebaseAuth.getInstance().getCurrentUser().getUid();
//        }
//        return null;
//    }
//}

package com.example.byway;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class PreferenceManager {

    private static final String PREF_NAME = "UserPrefs";           // 🔄 모든 SharedPreferences 통일
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_KAKAO_ID = "kakao_id";

    // ✅ 통합 로그인 여부 확인 함수 (카카오 OR 구글 중 하나만 되어 있어도 true)
    public static boolean isUserLoggedIn(Context context) {
        return FirebaseAuth.getInstance().getCurrentUser() != null || isKakaoLoggedIn(context);
    }

    // ✅ 카카오 로그인 여부 (SharedPreferences 기반)
    public static boolean isKakaoLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // ✅ 카카오 로그인 성공 시 저장
    public static void setKakaoLoggedIn(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply();
    }

    // ✅ 전체 로그아웃 시 SharedPreferences 초기화
    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    // 🔹 Kakao ID 저장
    public static void saveKakaoId(Context context, long kakaoId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_KAKAO_ID, kakaoId)
                .apply();
    }

    // 🔹 Kakao ID 가져오기
    public static long getCurrentKakaoId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_KAKAO_ID, -1);
    }

}
