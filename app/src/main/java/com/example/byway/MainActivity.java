package com.example.byway;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

//경로추천 import
import android.widget.EditText;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //공통 인자 (경로 탐색, 지도 표기)
    private MapView mapView;
    private NaverMap naverMap;

    //경로 탐색 인자
    private static final String CLIENT_ID = "b8amspex8g".trim();
    private static final String CLIENT_SECRET = "n3S8Tc6Tt88WDBC1qyJ8UBytr8Smq9cXmwSkvsWi".trim();


    private EditText startEditText;
    private EditText goalEditText;
    private TextView infoTextView;

    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Location lastLocation;
    //길 등록중인지
    private boolean isRecording = false;

    private PathRecorder pathRecorder;
    private MapManager mapManager;
    private Button startRecordButton;
    private Button finishRecordButton;
    private Button submitPathButton;
    private LinearLayout recordingControls;

    //경로 탐색 길 표기
    private PathOverlay currentPathOverlay; // <-- 새로 추가


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        startEditText = findViewById(R.id.startEditText);
        goalEditText = findViewById(R.id.endEditText);

        // 위치 소스 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        pathRecorder=new PathRecorder();

        //Tmap - 도보 경로 찾기 api
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // 전체 로그 (요청 URL, 헤더, 바디 등)

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {

            if (lastLocation == null) {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String goal = goalEditText.getText().toString();
            String start = startEditText.getText().toString();

            //Geocode 구현 : 주소로 검색하기

            Geocoder geocoder = new Geocoder(this);

            geocoder.geocodeAddress(start, (startLat, startLng) -> {
                geocoder.geocodeAddress(goal, (endLat, endLng) -> {
                    requestTmapWalkRoute(startLng, startLat, endLng, endLat);
                });
            });

            // 여기선 테스트용으로 위도,경도 직접 입력 받는다고 가정
            /*try {
                String[] goals = goal.split(",");
                double endLat = Double.parseDouble(goals[0].trim());
                double endLng = Double.parseDouble(goals[1].trim());

                String[] starts = start.split(",");
                double startLat = Double.parseDouble(starts[0].trim());
                double startLng = Double.parseDouble(starts[1].trim());

                requestTmapWalkRoute(startLat, startLng, endLat, endLng);
            } catch (Exception e) {
                Toast.makeText(this, "도착지를 '위도,경도' 형식으로 입력하세요.", Toast.LENGTH_SHORT).show();
            }*/
        });

        setupUI();
    }

    private void requestTmapWalkRoute(double startX, double startY, double endX, double endY) {
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
                                            //Log.d("route", "lat: " + lat + ", lng: " + lng);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (latLngList.size() >= 2) {

                        //Log.d("draw", "draw");

                        if (currentPathOverlay != null) {
                            currentPathOverlay.setMap(null); // 기존 경로 제거
                        }

                        currentPathOverlay = new PathOverlay();
                        currentPathOverlay.setCoords(latLngList);
                        currentPathOverlay.setColor(Color.BLUE);
                        currentPathOverlay.setWidth(12);
                        currentPathOverlay.setMap(naverMap);

                        /*PathOverlay pathOverlay = new PathOverlay();
                        pathOverlay.setCoords(latLngList);
                        pathOverlay.setColor(Color.BLUE);
                        pathOverlay.setWidth(12);
                        pathOverlay.setMap(naverMap);*/

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : latLngList) {
                            builder.include(latLng);
                        }
                        LatLngBounds bounds = builder.build();
                        naverMap.moveCamera(CameraUpdate.fitBounds(bounds, 100));
                    } else {
                        Toast.makeText(MainActivity.this, "경로가 충분하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "TMAP 응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TmapResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "TMAP 요청 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }



    private void setupUI() {
        ImageButton locationButton = findViewById(R.id.my_location_button);
        startRecordButton = findViewById(R.id.start_record_button); //경로 저장(등록 프로세스 시작버튼)
        finishRecordButton = findViewById(R.id.finish_record_button); //저장 끝내기
        submitPathButton = findViewById(R.id.submit_path_button); //경로 등록
        recordingControls = findViewById(R.id.recording_controls); //저장끝내기, 경로 등록하기 묶음
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //현위치 눌렀을때
        locationButton.setOnClickListener(v -> {
            if (naverMap == null || lastLocation == null) return;

            LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            LatLng center = naverMap.getCameraPosition().target;

            double distance = currentLatLng.distanceTo(center);
            if (distance > 100) {
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng));
            } else {
                LocationTrackingMode currentMode = naverMap.getLocationTrackingMode();
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });


        // BottomNavigationView 메뉴 클릭 리스너 설정
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_byway) {
                if (!isRecording) {
                    isRecording = true;
                    startRecordButton.setVisibility(View.GONE);
                    recordingControls.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "길 등록 시작!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            // 다른 탭 처리 예시 (선택적)
            else if (itemId == R.id.nav_nearby) {
                Toast.makeText(this, "주변 버튼 눌림", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_my) {
                Toast.makeText(this, "MY 버튼 눌림", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });


        //경로 저장 버튼 눌렀을때
        startRecordButton.setOnClickListener(v -> {
            if (isRecording) return;
            isRecording = true;

            //저장끝내기, 경로 등록 묶음 보이게
            startRecordButton.setVisibility(View.GONE);
            recordingControls.setVisibility(View.VISIBLE);
            Toast.makeText(this, "길 등록 시작!", Toast.LENGTH_SHORT).show();
        });

        //등록 끝내기 버튼 눌렀을때
        finishRecordButton.setOnClickListener(v -> {
            if (!isRecording) return;
            isRecording = false;
            pathRecorder.clear();
            mapManager.clearPath();

            //처음 화면으로
            startRecordButton.setVisibility(View.VISIBLE);
            recordingControls.setVisibility(View.GONE);
            Toast.makeText(this, "길 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        //경로 등록 눌렀을때
        submitPathButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("경로 등록")
                    .setMessage("현재까지의 경로를 등록하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Toast.makeText(MainActivity.this, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();


                        isRecording = false;
                        pathRecorder.clear();
                        mapManager.clearPath();

                        //맨 처음 화면으로
                        startRecordButton.setVisibility(View.VISIBLE);
                        recordingControls.setVisibility(View.GONE);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        mapManager = new MapManager(naverMap);
        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        naverMap.addOnLocationChangeListener(location -> {
            lastLocation = location;
            if (isRecording) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                pathRecorder.addPoint(latLng);
                mapManager.updatePath(pathRecorder.getPath());
            }
        });
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (naverMap != null) {
                if (locationSource.isActivated()) {
                    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                } else {
                    naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 생명주기 관리
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}