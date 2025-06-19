package com.example.byway;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;

import java.util.*;

public class PathUploader {

    public static void uploadPath(Context context, String keyword, List<LatLng> path) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. LatLng 리스트를 Firestore에 저장 가능한 형태로 변환
        List<Map<String, Double>> converted = new ArrayList<>();
        for (LatLng point : path) {
            Map<String, Double> coord = new HashMap<>();
            coord.put("lat", point.latitude);
            coord.put("lng", point.longitude);
            converted.add(coord);
        }

        // ✅ 2. 사용자 UID 가져오기 (Google/Kakao 통합)
        String uid = UserManager.getUid(context);
        if (uid == null) {
            Toast.makeText(context, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
            Log.e("PathUploader", "UID is null!");
            return; // ❗ 종료
        } else {
            Log.d("PathUploader", "User id: " + uid);
        }

        // 3. Firestore에 저장할 데이터 구성
        Map<String, Object> data = new HashMap<>();
        data.put("path", converted);            // 경로 좌표
        data.put("createdAt", new Date());      // 등록 시간
        data.put("createdBy", uid);             // 사용자 UID (등록자 정보)
        data.put("keyword", keyword);
        // 4. Firestore 업로드
        Log.d("PathUploader", "Converted path size: " + converted.size());

        db.collection("paths")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(context, "경로 등록 완료!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "ID: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "경로 등록 실패", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "에러", e);
                });
    }
}