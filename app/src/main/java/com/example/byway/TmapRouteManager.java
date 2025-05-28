package com.example.byway;

import android.graphics.Color;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.PathOverlay;

import java.util.ArrayList;
import java.util.List;

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

    public TmapRouteManager(MainActivity activity, NaverMap naverMap) {
        this.activity = activity;
        this.naverMap = naverMap;
        this.currentPathOverlay = null;
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
}