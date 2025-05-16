package com.example.byway;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;

public class UIController {

    private final MainActivity activity;
    private final NaverMap naverMap;
    private final MapManager mapManager;
    private final SpotManager spotManager;

    public UIController(MainActivity activity, NaverMap naverMap, MapManager mapManager, SpotManager spotManager) {
        this.activity = activity;
        this.naverMap = naverMap;
        this.mapManager = mapManager;
        this.spotManager = spotManager;
    }

    public void setupUI() {
        ImageButton locationButton = activity.findViewById(R.id.my_location_button);
        Button startRecordButton = activity.findViewById(R.id.start_record_button);
        Button finishRecordButton = activity.findViewById(R.id.finish_record_button);
        Button submitPathButton = activity.findViewById(R.id.submit_path_button);
        Button addSpotButton = activity.findViewById(R.id.add_spot_button);
        Button cancelSpotButton = activity.findViewById(R.id.cancel_spot_button);
        Button selectSpotButton = activity.findViewById(R.id.select_spot_button);
        LinearLayout recordingControls = activity.findViewById(R.id.recording_controls);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

        // 현위치 버튼
        locationButton.setOnClickListener(v -> {
            if (activity.getLastLocation() == null) return;

            LatLng currentLatLng = new LatLng(activity.getLastLocation().getLatitude(), activity.getLastLocation().getLongitude());
            LatLng center = naverMap.getCameraPosition().target;

            double distance = currentLatLng.distanceTo(center);
            if (distance > 100) {
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng));
            } else {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });

        // 길 등록 시작
        startRecordButton.setOnClickListener(v -> {
            if (activity.isRecording()) return;

            activity.setRecording(true);
            startRecordButton.setVisibility(View.GONE);
            recordingControls.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "길 등록 시작!", Toast.LENGTH_SHORT).show();
        });

        // 경로 등록 취소
        finishRecordButton.setOnClickListener(v -> {
            if (!activity.isRecording()) return;

            activity.setRecording(false);
            activity.getPathRecorder().clear();
            mapManager.clearPath();

            startRecordButton.setVisibility(View.VISIBLE);
            recordingControls.setVisibility(View.GONE);

            Toast.makeText(activity, "길 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 경로 등록 완료
        submitPathButton.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("경로 등록")
                    .setMessage("현재까지의 경로를 등록하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Toast.makeText(activity, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();

                        activity.setRecording(false);
                        activity.getPathRecorder().clear();
                        mapManager.clearPath();

                        startRecordButton.setVisibility(View.VISIBLE);
                        recordingControls.setVisibility(View.GONE);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });

        // 스팟 등록 시작
        addSpotButton.setOnClickListener(v -> {
            activity.setAddingSpot(true);
            activity.setSelectedSpot(null);

            addSpotButton.setVisibility(View.GONE);
            cancelSpotButton.setVisibility(View.VISIBLE);
            selectSpotButton.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "스팟을 지도에서 선택하세요", Toast.LENGTH_SHORT).show();
        });

        // 스팟 선택 완료
        selectSpotButton.setOnClickListener(v -> {
            LatLng selectedSpot = activity.getSelectedSpot();
            if (selectedSpot == null) {
                Toast.makeText(activity, "스팟을 먼저 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(activity, SpotActivity.class);
            intent.putExtra("latitude", selectedSpot.latitude);
            intent.putExtra("longitude", selectedSpot.longitude);
            activity.getSpotActivityLauncher().launch(intent);
        });

        // 스팟 등록 취소
        cancelSpotButton.setOnClickListener(v -> {
            spotManager.clearSpotMarker();
            activity.setAddingSpot(false);

            cancelSpotButton.setVisibility(View.GONE);
            selectSpotButton.setVisibility(View.GONE);
            addSpotButton.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "스팟 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 하단 바
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_byway) {
                if (!activity.isRecording()) {
                    activity.setRecording(true);
                    startRecordButton.setVisibility(View.GONE);
                    recordingControls.setVisibility(View.VISIBLE);
                    Toast.makeText(activity, "길 등록 시작!", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_nearby) {
                Toast.makeText(activity, "주변 버튼 눌림", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_my) {
                Toast.makeText(activity, "MY 버튼 눌림", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }
}
