package com.example.byway;

import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.example.byway.searchPOI.KakaoResponse;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.PathOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TmapRouteManager {

    private final MainActivity activity;
    private final NaverMap naverMap;
    private PathOverlay currentPathOverlay;

    private final List<List<LatLng>> mergedBywayRoutes = new ArrayList<>();
    private final List<RouteInfo> routeInfoList = new ArrayList<>();

    public TmapRouteManager(MainActivity activity, NaverMap naverMap) {
        this.activity = activity;
        this.naverMap = naverMap;
        this.currentPathOverlay = null;
    }

    private void drawPathOnMap(List<LatLng> path, String name) {
        PathOverlay pathOverlay = new PathOverlay();
        pathOverlay.setCoords(path);
        pathOverlay.setColor(Color.GREEN);
        pathOverlay.setWidth(10);
        pathOverlay.setMap(naverMap);
    }

    private void processAndMergeByway(List<LatLng> tmapPath, List<List<LatLng>> bywayPaths) {
        LatLng tmapStart = tmapPath.get(0);
        LatLng tmapEnd = tmapPath.get(tmapPath.size() - 1);

        for (List<LatLng> bywayPath : bywayPaths) {

            if (bywayPath.isEmpty()) continue;

            LatLng bywayStart = bywayPath.get(0);
            LatLng bywayEnd = bywayPath.get(bywayPath.size() - 1);

            requestPartialTmapRoute(tmapStart, bywayStart, routeToByway -> {

                // [3] 샛길 끝점 → Tmap 끝점 경로 요청
                requestPartialTmapRoute(bywayEnd, tmapEnd, routeFromByway -> {

                    // 전체 경로 병합
                    List<LatLng> merged = new ArrayList<>();
                    merged.addAll(routeToByway);
                    merged.addAll(bywayPath);
                    merged.addAll(routeFromByway);

                    // 병합된 경로들 저장
                    mergedBywayRoutes.add(merged);

                    // 거리 및 시간 계산
                    double totalDistance = calculateTotalDistance(merged);
                    int estimatedTime = estimateWalkingTimeMinutes(totalDistance);

                    // UI에 사용할 경로 정보 저장
                    routeInfoList.add(new RouteInfo(merged, totalDistance, estimatedTime));

                });
            });
        }
        activity.updateRouteListUI(routeInfoList);

        for (List<LatLng> mergedBywayRoute : mergedBywayRoutes) {
            PathOverlay pathOverlay = new PathOverlay();
            pathOverlay.setCoords(mergedBywayRoute);
            pathOverlay.setColor(Color.RED);
            pathOverlay.setWidth(10);
            pathOverlay.setMap(naverMap);
        }
    }

    private void requestPartialTmapRoute(LatLng start, LatLng end, RouteCallback callback) {
        TmapWalkRequest request = new TmapWalkRequest(
                String.valueOf(start.longitude),
                String.valueOf(start.latitude),
                String.valueOf(end.longitude),
                String.valueOf(end.latitude)
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.openapi.sk.com/tmap/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TmapApiService apiService = retrofit.create(TmapApiService.class);

        Call<TmapResponse> call = apiService.getWalkingRoute(
                "iwCg3TsBeQ35D1YwoVsYm9yaP1NT6UtW3vXIRpEK", // 본인 AppKey
                request
        );

        call.enqueue(new Callback<TmapResponse>() {
            @Override
            public void onResponse(Call<TmapResponse> call, Response<TmapResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LatLng> latLngList = new ArrayList<>();
                    for (TmapResponse.Feature feature : response.body().features) {
                        if (feature.geometry != null && "LineString".equals(feature.geometry.type)) {
                            List<?> coordList = (List<?>) feature.geometry.coordinates;
                            for (Object coord : coordList) {
                                if (coord instanceof List) {
                                    List<?> point = (List<?>) coord;
                                    if (point.size() >= 2 && point.get(0) instanceof Number && point.get(1) instanceof Number) {
                                        double lng = ((Number) point.get(0)).doubleValue();
                                        double lat = ((Number) point.get(1)).doubleValue();
                                        latLngList.add(new LatLng(lat, lng));
                                    }
                                }
                            }
                        }
                    }

                    callback.onRouteReceived(latLngList);

                } else {
                    Log.e("TmapPartialRoute", "응답 실패");
                }
            }

            @Override
            public void onFailure(Call<TmapResponse> call, Throwable t) {
                Log.e("TmapPartialRoute", "요청 실패", t);
            }
        });
    }

    interface RouteCallback {
        void onRouteReceived(List<LatLng> route);
    }

    private double calculateTotalDistance(List<LatLng> path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += getDistance(path.get(i), path.get(i + 1));
        }
        return total; // meters
    }

    private int estimateWalkingTimeMinutes(double distanceInMeters) {
        double averageSpeed = 66.0; // m/min (약 4km/h)
        return (int) Math.round(distanceInMeters / averageSpeed);
    }


    //두 지점 거리 계산 함수
    private double getDistance(LatLng latLng1, LatLng latLng2) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double dLng = Math.toRadians(latLng2.longitude - latLng1.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    //latLngList 기준으로 특정 path가 반경 300m 이내인지 검사
    private boolean isWithin300m(List<LatLng> basePath, List<LatLng> otherPath) {
        for (LatLng base : basePath) {
            for (LatLng other : otherPath) {
                if (getDistance(base, other) <= 300.0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void requestTmapWalkRoute(double startX, double startY, double endX, double endY) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 전체 로그 출력

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.openapi.sk.com/tmap/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TmapApiService apiService = retrofit.create(TmapApiService.class);

        TmapWalkRequest request = new TmapWalkRequest(
                String.valueOf(startX),
                String.valueOf(startY),
                String.valueOf(endX),
                String.valueOf(endY)
        );

        Call<TmapResponse> call = apiService.getWalkingRoute(
                "iwCg3TsBeQ35D1YwoVsYm9yaP1NT6UtW3vXIRpEK", // <- 반드시 본인 AppKey
                request
        );

        call.enqueue(new Callback<TmapResponse>() {
            @Override
            public void onResponse(Call<TmapResponse> call, Response<TmapResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LatLng> latLngList = new ArrayList<>();

                    for (TmapResponse.Feature feature : response.body().features) {
                        if (feature.geometry != null && feature.geometry.type.equals("LineString")) {
                            Object coordinatesObj = feature.geometry.coordinates;

                            if (coordinatesObj instanceof List) {
                                List<?> coordList = (List<?>) coordinatesObj;

                                for (Object coord : coordList) {
                                    if (coord instanceof List) {
                                        List<?> point = (List<?>) coord;

                                        if (point.size() >= 2 && point.get(0) instanceof Number && point.get(1) instanceof Number) {
                                            double lng = ((Number) point.get(0)).doubleValue();
                                            double lat = ((Number) point.get(1)).doubleValue();

                                            LatLng latLng = new LatLng(lat, lng);
                                            latLngList.add(latLng);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (latLngList.size() >= 2) {

                        if (currentPathOverlay != null) {
                            currentPathOverlay.setMap(null); // 기존 경로 제거
                        }

                        currentPathOverlay = new PathOverlay();
                        currentPathOverlay.setCoords(latLngList);
                        currentPathOverlay.setColor(Color.BLUE);
                        currentPathOverlay.setWidth(12);
                        currentPathOverlay.setMap(naverMap);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : latLngList) {
                            builder.include(latLng);
                        }
                        LatLngBounds bounds = builder.build();
                        naverMap.moveCamera(CameraUpdate.fitBounds(bounds, 100));

                        //주변(300m) 샛길 불러오기 및 샛길 연결하기
                        fetchAndProcessByways(latLngList);
                    } else {
                        Toast.makeText(activity, "경로가 충분하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "TMAP 응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TmapResponse> call, Throwable t) {
                Toast.makeText(activity, "TMAP 요청 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchAndProcessByways(List<LatLng> tmapPath) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<List<LatLng>> allBywayPaths = new ArrayList<>();

        db.collection("paths")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");

                        List<Map<String, Object>> points = (List<Map<String, Object>>) doc.get("path");

                        if (points != null) {
                            List<LatLng> latLngPathList = new ArrayList<>();
                            for (Map<String, Object> point : points) {
                                if (point != null) {
                                    Object latObj = point.get("lat");
                                    Object lngObj = point.get("lng");

                                    if (latObj instanceof Number && lngObj instanceof Number) {
                                        double lat = ((Number) latObj).doubleValue();
                                        double lng = ((Number) lngObj).doubleValue();
                                        latLngPathList.add(new LatLng(lat, lng));
                                    } else {
                                        Log.w("FireStore", "Invalid lat/lng type in document " + doc.getId());
                                    }
                                } else {
                                    Log.w("FireStore", "Null point in points list, doc " + doc.getId());
                                }
                            }
                            if (!latLngPathList.isEmpty() && isWithin300m(tmapPath, latLngPathList)) {
                                //300m 이내 샛길만 추가
                                allBywayPaths.add(latLngPathList);

                                //주변(300m) 샛길 경로 그리기
                                drawPathOnMap(latLngPathList, name);

                            } else {
                                Log.w("FireStore", "latLngPathList is empty for doc " + doc.getId());
                            }

                        } else {
                            Log.e("FireStore", "points 필드가 null입니다. doc id: " + doc.getId());
                        }
                    }
                    //받은 샛길 정보를 경유하도록 하는 로직
                    processAndMergeByway(tmapPath, allBywayPaths);

                })
                .addOnFailureListener(e -> {
                    Log.e("FireStore", "Failed to load path", e);
                });
    }
}