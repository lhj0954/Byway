package com.example.byway;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.byway.searchPOI.POIAdapter;
import com.example.byway.searchPOI.SearchTextWatcher;
import com.example.byway.Geocoder;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;


import java.util.ArrayList;
import java.util.List;


public class UIController {

    private final MainActivity activity;
    private final NaverMap naverMap;
    private final MapManager mapManager;
    private final SpotManager spotManager;
    private final TmapRouteManager tmapRouteManager;
    private RecyclerView resultRecycler;
    private POIAdapter adapter;
    private Double startPointLat = null,startPointLng = null,searchInputLat = null,searchInputLng = null;
    private SearchTextWatcher startPointWatcher;
    private SearchTextWatcher searchInputWatcher;
    private EditText startPoint;

    public SearchTextWatcher getStartPointWatcher() { return startPointWatcher; }
    public SearchTextWatcher getSearchInputWatcher() { return searchInputWatcher; }


    public void setStartPointLat(Double startPointLat) {
        this.startPointLat = startPointLat;
    }

    public void setStartPointLng(Double startPointLng) {
        this.startPointLng = startPointLng;
    }

    public void setSearchInputLat(Double searchInputLat) {
        this.searchInputLat = searchInputLat;
    }

    public void setSearchInputLng(Double searchInputLng) {
        this.searchInputLng = searchInputLng;
    }

    public UIController(MainActivity activity, NaverMap naverMap, MapManager mapManager, SpotManager spotManager) {
        this.activity = activity;
        this.naverMap = naverMap;
        this.mapManager = mapManager;
        this.spotManager = spotManager;
        this.tmapRouteManager = new TmapRouteManager(activity, naverMap);
        this.startPointLat = null;
        this.startPointLng = null;

        if (activity.getLastLocation() != null) {
            this.startPointLat = activity.getLastLocation().getLatitude();
            this.startPointLng = activity.getLastLocation().getLongitude();
        }
    }



    public void setupUI() {
        ImageButton locationButton = activity.findViewById(R.id.my_location_button);
        Button finishRecordButton = activity.findViewById(R.id.finish_record_button);
        Button submitPathButton = activity.findViewById(R.id.submit_path_button);
        Button cancelSpotButton = activity.findViewById(R.id.cancel_spot_button);
        Button selectSpotButton = activity.findViewById(R.id.select_spot_button);
        Button searchButton = activity.findViewById(R.id.searchButton);
        LinearLayout recordingControls = activity.findViewById(R.id.recording_controls);
        LinearLayout fabSubContainer = activity.findViewById(R.id.fab_sub_container);
        FloatingActionButton fabSubLeft = activity.findViewById(R.id.fab_sub_left);
        FloatingActionButton fabSubRight = activity.findViewById(R.id.fab_sub_right);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        CardView bottomBarCard = activity.findViewById(R.id.bottom_bar_card);
        LinearLayout pathControls = activity.findViewById(R.id.path_controls);
        LinearLayout spotControls = activity.findViewById(R.id.spot_controls);
        Spinner categorySpinner = activity.findViewById(R.id.category_spinner);
        startPoint = activity.findViewById(R.id.start_point);
        EditText searchInput = activity.findViewById(R.id.search_input);
        resultRecycler = activity.findViewById(R.id.search_results_recycler);

        // RecyclerView 설정
        resultRecycler.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new POIAdapter(new ArrayList<>(), activity, startPoint, searchInput, resultRecycler, this,
                (lat, lng, name) -> {
                    if (startPoint.hasFocus()) {
                        startPointLat = lat;
                        startPointLng = lng;
                    } else {
                        searchInputLat = lat;
                        searchInputLng = lng;
                    }
                }); // POIAdapter 초기화
        resultRecycler.setAdapter(adapter); // RecyclerView에 adapter 설정

        startPointWatcher = new SearchTextWatcher(activity, adapter, resultRecycler,true, this);
        searchInputWatcher = new SearchTextWatcher(activity, adapter, resultRecycler,false, this);
        startPoint.addTextChangedListener(
                startPointWatcher
        );

        searchInput.addTextChangedListener(searchInputWatcher);

        searchButton.setOnClickListener(v -> {
            String start = startPoint.getText().toString().trim();
            String goal = searchInput.getText().toString().trim();

            Geocoder geocoder = new Geocoder(activity);

            boolean isStartReady = startPointLat != null && startPointLng != null;
            boolean isGoalReady = searchInputLat != null && searchInputLng != null;


            if (!isStartReady && !isGoalReady) {
                //맨 처음 현위치가 시작점
                if(startPoint.getText().toString().isEmpty()) {
                    System.out.println("현위치시작. lat : " + startPointLat + "lng :" + startPointLng);
                    System.out.println("현위치시작. lat : " + searchInputLat + "lng :" + searchInputLng);
                    geocoder.geocodeAddress(goal, (endLat, endLng) -> {
                        tmapRouteManager.requestTmapWalkRoute(activity.getLastLocation().getLongitude(), activity.getLastLocation().getLatitude(), endLng, endLat);
                    });
                }
                // 둘 다 텍스트 주소인 경우
                else {
                    System.out.println("둘다. lat : " + startPointLat + "lng :" + startPointLng);
                    System.out.println("둘다. lat : " + searchInputLat + "lng :" + searchInputLng);
                    geocoder.geocodeAddress(start, (startLat, startLng) -> {
                        geocoder.geocodeAddress(goal, (endLat, endLng) -> {
                            tmapRouteManager.requestTmapWalkRoute(startLng, startLat, endLng, endLat);
                        });
                    });
                }
            } else if (!isStartReady) {
                //맨 처음 현위치가 시작점
                if(startPoint.getText().toString().isEmpty()){
                    System.out.println("현위치시작. lat : " + startPointLat + "lng :" + startPointLng);
                    System.out.println("현위치시작. lat : " + searchInputLat + "lng :" + searchInputLng);
                    tmapRouteManager.requestTmapWalkRoute(activity.getLastLocation().getLongitude(), activity.getLastLocation().getLatitude(), searchInputLng, searchInputLat);
                }
                // 시작만 텍스트 주소인 경우
                else {
                    System.out.println("시작만. lat : " + startPointLat + "lng :" + startPointLng);
                    System.out.println("시작만. lat : " + searchInputLat + "lng :" + searchInputLng);
                    geocoder.geocodeAddress(start, (startLat, startLng) -> {
                        tmapRouteManager.requestTmapWalkRoute(startLng, startLat, searchInputLng, searchInputLat);
                    });
                }
            } else if (!isGoalReady) {
                // 도착지만 텍스트 주소인 경우
                System.out.println("도착만. lat : "+startPointLat+"lng :"+startPointLng);
                System.out.println("도착만. lat : "+searchInputLat+"lng :"+searchInputLng);
                geocoder.geocodeAddress(goal, (endLat, endLng) -> {
                    tmapRouteManager.requestTmapWalkRoute(startPointLng, startPointLat, endLng, endLat);
                });
            } else {
                // 둘 다 좌표값 있을 때
                System.out.println("lat : "+startPointLat+"lng :"+startPointLng);
                System.out.println("lat : "+searchInputLat+"lng :"+searchInputLng);
                tmapRouteManager.requestTmapWalkRoute(startPointLng, startPointLat, searchInputLng, searchInputLat);
            }
        });

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

        // 기본 하단 바 세팅
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // 경로 등록 취소
        finishRecordButton.setOnClickListener(v -> {
            if (!activity.isRecording()) return;

            activity.setRecording(false);
            activity.getPathRecorder().clear();
            mapManager.clearPath();

            recordingControls.setVisibility(GONE);
            pathControls.setVisibility(GONE);
            spotControls.setVisibility(GONE);

            bottomBarCard.setVisibility(View.VISIBLE);

            categorySpinner.setSelection(0);


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

                        if (selectedCategory == null || selectedCategory.equals("카테고리")) {
                            Toast.makeText(activity, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // PathRecorder에서 경로 데이터 가져오기
                        List<LatLng> pathPoints = activity.getPathRecorder().getPath();
                        if (pathPoints == null || !activity.getPathRecorder().hasEnoughPoints()) {
                            Toast.makeText(activity, "경로 데이터가 충분하지 않습니다. 최소 2개의 지점이 필요합니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //db 업로드
                        PathUploader.uploadPath(activity, pathPoints);

                        Toast.makeText(activity, "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();

                        activity.setRecording(false);
                        activity.getPathRecorder().clear();
                        mapManager.clearPath();

                        recordingControls.setVisibility(GONE);
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

            recordingControls.setVisibility(GONE);
            spotControls.setVisibility(GONE);

            pathControls.setVisibility(GONE);

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
            }
            // 로그인 체크 후 My 클릭 시
            else if (itemId == R.id.nav_my) {
                Intent intent;
                if (PreferenceManager.isUserLoggedIn(activity)) {
                    intent = new Intent(activity, MypageActivity.class); // 로그인 되어 있을 경우 Mypage
                } else {
                    Toast.makeText(activity, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
                    intent = new Intent(activity, LoginActivity.class); // 로그인 안되어 있을 경우 Login
                }
                activity.startActivity(intent);
                return true;
            }

            return false;
        });

        // 오른쪽 FAB 클릭 시 샛길 등록 시작
        fabSubRight.setOnClickListener(v -> {
            if (!PreferenceManager.isUserLoggedIn(activity)) {
                Toast.makeText(activity, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                return;
            }

            if (!activity.isRecording()) {
                activity.setRecording(true);

                recordingControls.setVisibility(View.VISIBLE);
                pathControls.setVisibility(View.VISIBLE);


                spotControls.setVisibility(GONE); // 스팟 버튼 숨김
                bottomBarCard.setVisibility(GONE);

                FabManager.hideSubFabs(fabSubContainer);
                activity.setFabOpen(false);

                Toast.makeText(activity, "길 등록 시작!", Toast.LENGTH_SHORT).show();
            }
        });

        //스팟 등록
        fabSubLeft.setOnClickListener(v -> {
            if (!PreferenceManager.isUserLoggedIn(activity)) {
                Toast.makeText(activity, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                return;
            }

            activity.setAddingSpot(true);
            activity.setSelectedSpot(null);

            recordingControls.setVisibility(View.VISIBLE);
            pathControls.setVisibility(GONE);  // 경로 버튼 숨김
            spotControls.setVisibility(View.VISIBLE);

            bottomBarCard.setVisibility(GONE);
            FabManager.hideSubFabs(fabSubContainer);
            activity.setFabOpen(false);

            Toast.makeText(activity, "스팟을 지도에서 선택하세요", Toast.LENGTH_SHORT).show();
        });
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
            // 1. 스팟 등록 모드일 때
            if (activity.isAddingSpot()) {
                spotManager.addSpotMarker(naverMap, latLng);
                activity.setSelectedSpot(latLng);
                return;
            }
            // 2. 사진 명소 추천 모드일 때
            else if (activity.isViewingSpots()) {
                activity.getIntent().removeExtra("photo_spots");
                activity.setViewingSpots(false);
                spotManager.clearAllSpotMarkers();
                // (선택적으로 바텀시트 닫기 등 추가 작업 가능)
            }
        });
    }
}
