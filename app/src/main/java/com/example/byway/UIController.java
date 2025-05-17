package com.example.byway;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;

import java.util.List;


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
        Button finishRecordButton = activity.findViewById(R.id.finish_record_button);
        Button submitPathButton = activity.findViewById(R.id.submit_path_button);
        Button cancelSpotButton = activity.findViewById(R.id.cancel_spot_button);
        Button selectSpotButton = activity.findViewById(R.id.select_spot_button);
        LinearLayout recordingControls = activity.findViewById(R.id.recording_controls);
        LinearLayout fabSubContainer = activity.findViewById(R.id.fab_sub_container);
        FloatingActionButton fabSubLeft = activity.findViewById(R.id.fab_sub_left);
        FloatingActionButton fabSubRight = activity.findViewById(R.id.fab_sub_right);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        CardView bottomBarCard = activity.findViewById(R.id.bottom_bar_card);
        LinearLayout pathControls = activity.findViewById(R.id.path_controls);
        LinearLayout spotControls = activity.findViewById(R.id.spot_controls);
        Spinner categorySpinner = activity.findViewById(R.id.category_spinner);

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


        // 경로 등록 취소
        finishRecordButton.setOnClickListener(v -> {
            if (!activity.isRecording()) return;

            activity.setRecording(false);
            activity.getPathRecorder().clear();
            mapManager.clearPath();

            recordingControls.setVisibility(View.GONE);
            pathControls.setVisibility(View.GONE);
            spotControls.setVisibility(View.GONE);


            bottomBarCard.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "길 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 경로 등록 완료
        submitPathButton.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("경로 등록")
                    .setMessage("현재까지의 경로를 등록하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        String selectedCategory = categorySpinner.getSelectedItem() != null
                                ? categorySpinner.getSelectedItem().toString()
                                : null;

                        if (selectedCategory == null) {
                            Toast.makeText(activity, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // PathRecorder에서 경로 데이터 가져오기
                        List<LatLng> pathPoints = activity.getPathRecorder().getPath();
                        if (pathPoints == null || !activity.getPathRecorder().hasEnoughPoints()) {
                            Toast.makeText(activity, "경로 데이터가 충분하지 않습니다. 최소 2개의 지점이 필요합니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("MainActivity", "pathPoints size: " + pathPoints.size());
                        for (int i = 0; i < pathPoints.size(); i++) {
                            LatLng point = pathPoints.get(i);
                            Log.d("MainActivity",  "" + point.latitude + ", " + point.longitude);
                        }

                        Toast.makeText(activity, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();

                        activity.setRecording(false);
                        activity.getPathRecorder().clear();
                        mapManager.clearPath();

                        recordingControls.setVisibility(View.GONE);
                        bottomBarCard.setVisibility(View.VISIBLE);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
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

            recordingControls.setVisibility(View.GONE);
            spotControls.setVisibility(View.GONE);

            pathControls.setVisibility(View.GONE);

            bottomBarCard.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "스팟 등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 기존 샛길 버튼 (하단 메뉴용 - 현재는 숨기거나 사용 안 할 예정)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_add) {
                if (!activity.isFabOpen()) {
                    FabManager.showSubFabs(fabSubContainer);
                } else {
                    FabManager.hideSubFabs(fabSubContainer);
                }
                activity.setFabOpen(!activity.isFabOpen());
                return false;
            } else if (itemId == R.id.nav_nearby) {
                NearbyBottomSheetFragment bottomSheet = new NearbyBottomSheetFragment();
                bottomSheet.show(activity.getSupportFragmentManager(), bottomSheet.getTag());
                return false;
            } else if (itemId == R.id.nav_my) {
                Toast.makeText(activity, "MY 버튼 눌림", Toast.LENGTH_SHORT).show();
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

        // 오른쪽 FAB 클릭 시 샛길 등록 시작
        fabSubRight.setOnClickListener(v -> {
            if (!activity.isRecording()) {
                activity.setRecording(true);

                recordingControls.setVisibility(View.VISIBLE);
                pathControls.setVisibility(View.VISIBLE);


                spotControls.setVisibility(View.GONE); // 스팟 버튼 숨김
                bottomBarCard.setVisibility(View.GONE);

                FabManager.hideSubFabs(fabSubContainer);
                activity.setFabOpen(false);

                Toast.makeText(activity, "길 등록 시작!", Toast.LENGTH_SHORT).show();
            }
        });

        //스팟 등록
        fabSubLeft.setOnClickListener(v -> {
            activity.setAddingSpot(true);
            activity.setSelectedSpot(null);

            recordingControls.setVisibility(View.VISIBLE);
            pathControls.setVisibility(View.GONE);  // 경로 버튼 숨김
            spotControls.setVisibility(View.VISIBLE);

            bottomBarCard.setVisibility(View.GONE);
            FabManager.hideSubFabs(fabSubContainer);
            activity.setFabOpen(false);

            Toast.makeText(activity, "스팟을 지도에서 선택하세요", Toast.LENGTH_SHORT).show();
        });


        // 기본 하단 바 세팅
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
    }

    //Map 리스너
    public void setupMapListeners() {
        naverMap.addOnLocationChangeListener(location -> {
            activity.setLastLocation(location);  // lastLocation이 activity에 있다면 이렇게 처리
            if (activity.isRecording()) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                activity.getPathRecorder().addPoint(latLng);
                mapManager.updatePath(activity.getPathRecorder().getPath());
            }
        });

        naverMap.setOnMapClickListener((point, latLng) -> {
            if (!activity.isAddingSpot()) return;

            // 기존 마커가 있다면 제거하고 추가
            spotManager.addSpotMarker(naverMap, latLng);
            activity.setSelectedSpot(latLng);
        });
    }

}
