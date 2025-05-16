package com.example.byway;
import com.example.byway.utils.PreferenceManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; 
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private Location lastLocation;

    private boolean isRecording = false;
    private boolean isFabOpen = false;

    private PathRecorder pathRecorder;
    private MapManager mapManager;
    private Button startRecordButton;
    private Button finishRecordButton;
    private Button submitPathButton;

    private LinearLayout recordingControls;
    private LinearLayout fabSubContainer;

    private FloatingActionButton fabSubLeft, fabSubRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        pathRecorder = new PathRecorder();

        setupUI();
    }

    private void setupUI() {
        ImageButton locationButton = findViewById(R.id.my_location_button);
        startRecordButton = findViewById(R.id.start_record_button);
        finishRecordButton = findViewById(R.id.finish_record_button);
        submitPathButton = findViewById(R.id.submit_path_button);
        recordingControls = findViewById(R.id.recording_controls);
        fabSubContainer = findViewById(R.id.fab_sub_container);
        fabSubLeft = findViewById(R.id.fab_sub_left);
        fabSubRight = findViewById(R.id.fab_sub_right);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        CardView bottomBarCard = findViewById(R.id.bottom_bar_card);
        //startRecordButton 비활성화
        startRecordButton.setVisibility(View.GONE);

        // 오른쪽 FAB 클릭 시 샛길 등록 시작
        fabSubRight.setOnClickListener(v -> {
            if (!isRecording) {
                isRecording = true;
                startRecordButton.setVisibility(View.GONE);
                recordingControls.setVisibility(View.VISIBLE);
                bottomBarCard.setVisibility(View.GONE);
                hideSubFabs();
                isFabOpen = !isFabOpen;
                Toast.makeText(MainActivity.this, "길 등록 시작!", Toast.LENGTH_SHORT).show();
            }
        });

        // 기본 하단 바 세팅
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // 현재 위치 버튼
        locationButton.setOnClickListener(v -> {
            if (naverMap == null || lastLocation == null) return;
            LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            LatLng center = naverMap.getCameraPosition().target;
            double distance = currentLatLng.distanceTo(center);
            if (distance > 100) {
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng));
            } else {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });

        // 기존 샛길 버튼 (하단 메뉴용 - 현재는 숨기거나 사용 안 할 예정)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_add) {
                if (!isFabOpen) {
                    showSubFabs();
                } else {
                    hideSubFabs();
                }
                isFabOpen = !isFabOpen;
                return false;
            } else if (itemId == R.id.nav_nearby) {
                NearbyBottomSheetFragment bottomSheet = new NearbyBottomSheetFragment();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                return false;
            } else if (itemId == R.id.nav_my) {
                Toast.makeText(this, "MY 버튼 눌림", Toast.LENGTH_SHORT).show();
                return false;
            }
			/*
            // 로그인 체크 후 My 클릭 시
            else if (itemId == R.id.nav_my) {
                Intent intent;
                if (PreferenceManager.isLoggedIn(MainActivity.this)) {
                    intent = new Intent(MainActivity.this, MyPageActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                return true;
            }
             */
            return false;
        });

        // 경로 저장 버튼
        startRecordButton.setOnClickListener(v -> {
            if (isRecording){
                Toast.makeText(this, "길 등록 중입니다", Toast.LENGTH_SHORT).show();
                return;
            }
            isRecording = true;
            startRecordButton.setVisibility(View.GONE);
            recordingControls.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.GONE);
            Toast.makeText(this, "길 등록 시작!", Toast.LENGTH_SHORT).show();
        });

        // 저장 끝내기 버튼
        finishRecordButton.setOnClickListener(v -> {
            if (!isRecording) return;
            isRecording = false;
            pathRecorder.clear();
            mapManager.clearPath();
            recordingControls.setVisibility(View.GONE);
            bottomBarCard.setVisibility(View.VISIBLE);
            Toast.makeText(this, "길 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 경로 등록 버튼
        submitPathButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("경로 등록")
                    .setMessage("현재까지의 경로를 등록하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Toast.makeText(MainActivity.this, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        isRecording = false;
                        pathRecorder.clear();
                        mapManager.clearPath();
                        recordingControls.setVisibility(View.GONE);
                        bottomBarCard.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
    }
    // FAB 나타나는 애니메이션
    private void showSubFabs() {
        fabSubContainer.setVisibility(View.VISIBLE);
        fabSubContainer.setAlpha(0f);
        fabSubContainer.setScaleX(0.8f);
        fabSubContainer.setScaleY(0.8f);

        fabSubContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
    }

    // FAB 사라지는 애니메이션
    private void hideSubFabs() {
        fabSubContainer.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .withEndAction(() -> fabSubContainer.setVisibility(View.GONE))
                .start();
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

    // 생명주기
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
