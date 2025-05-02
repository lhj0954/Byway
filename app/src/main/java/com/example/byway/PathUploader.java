//package com.example.byway;
//
//import android.content.Context;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.naver.maps.geometry.LatLng;
//
//import java.util.*;
//
////db에 저장. 아직 사용X
//public class PathUploader {
//
//    public static void uploadPath(Context context, List<LatLng> path) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        List<Map<String, Double>> converted = new ArrayList<>();
//        for (LatLng point : path) {
//            Map<String, Double> coord = new HashMap<>();
//            coord.put("lat", point.latitude);
//            coord.put("lng", point.longitude);
//            converted.add(coord);
//        }
//
//        Map<String, Object> data = new HashMap<>();
//
//        db.collection("paths")
//                .add(data)
//                .addOnSuccessListener(docRef -> {
//                    Toast.makeText(context, "경로 등록 완료!", Toast.LENGTH_SHORT).show();
//                    Log.d("Firestore", "ID: " + docRef.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "경로 등록 실패", Toast.LENGTH_SHORT).show();
//                    Log.w("Firestore", "에러", e);
//                });
//    }
//}
