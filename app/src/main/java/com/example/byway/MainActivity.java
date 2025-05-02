package com.example.byway;

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
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 위치 소스 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        pathRecorder=new PathRecorder();

        setupUI();
    }

    private void setupUI() {
        ImageButton locationButton = findViewById(R.id.my_location_button);
        startRecordButton = findViewById(R.id.start_record_button);
        finishRecordButton = findViewById(R.id.finish_record_button);
        submitPathButton = findViewById(R.id.submit_path_button);
        recordingControls = findViewById(R.id.recording_controls);

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

        startRecordButton.setOnClickListener(v -> {
            if (isRecording) return;
            isRecording = true;
//            pathRecorder.clear();


            startRecordButton.setVisibility(View.GONE);
            recordingControls.setVisibility(View.VISIBLE);
            Toast.makeText(this, "길 등록 시작!", Toast.LENGTH_SHORT).show();
        });

        finishRecordButton.setOnClickListener(v -> {
            if (!isRecording) return;
            isRecording = false;
            pathRecorder.clear();
            mapManager.clearPath();

            startRecordButton.setVisibility(View.VISIBLE);
            recordingControls.setVisibility(View.GONE);
            Toast.makeText(this, "길 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        submitPathButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("경로 등록")
                    .setMessage("현재까지의 경로를 등록하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Toast.makeText(MainActivity.this, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();


                        isRecording = false;
                        pathRecorder.clear();
                        mapManager.clearPath();

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