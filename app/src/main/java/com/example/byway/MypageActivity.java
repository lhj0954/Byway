package com.example.byway;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.byway.mypage.MergedAdapter;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MypageActivity extends AppCompatActivity implements OnMapReadyCallback{

    private TextView nameText;
    private TextView logoutBtn;
    private Button showAllBtn;
    private MapView mapView;
    private NaverMap naverMap;
    private RecyclerView recyclerView;
    private final List<Object> mergedList = new ArrayList<>();
    private boolean pathsLoaded = false;
    private boolean spotsLoaded = false;
    private boolean showingAll = false;
    private MapManager mapManager;
    private SpotManager spotManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        nameText = findViewById(R.id.text_user_id);
        logoutBtn = findViewById(R.id.text_logout); // Mypage 로그아웃 버튼 id
        recyclerView = findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showAllBtn = findViewById(R.id.show_all_button);
        mapView = findViewById(R.id.map_view);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initMapView(savedInstanceState);
        }

//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            // 로그인 되어있지 않다면 MainActivity로 강제 이동
//            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//            return;
//        }

        fetchAndDisplayUserName();

        loadUserContent(UserManager.getUid(this));

        logoutBtn.setOnClickListener(v -> logoutAll(MypageActivity.this));

        showAllBtn.setOnClickListener(v->{
            if(showingAll){
                showAllBtn.setText("전체 내역 보기");
            }else{
                showAllBtn.setText("목록 숨기기");
            }
            showingAll = !showingAll;
            showMergedList();
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap map) {
        this.naverMap = map;

        mapManager = new MapManager(naverMap);
        spotManager = new SpotManager();

        // 기본 카메라 위치 (서울 등)
        LatLng defaultLocation = new LatLng(37.5665, 126.9780);
        naverMap.moveCamera(CameraUpdate.scrollTo(defaultLocation));

        // 위치 권한이 허용된 상태라면, 위치 추적 활성화
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // FusedLocationSource 생성
            FusedLocationSource locationSource =
                    new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

            naverMap.setLocationSource(locationSource);
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        }
    }

    private void fetchAndDisplayUserName() {
        String uid = UserManager.getUid(this);

        if (uid == null) {
            nameText.setText("⚙ 로그인 정보 없음");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null) {
                            nameText.setText("⚙ " + name + " 님");
                        } else {
                            nameText.setText("⚙ 이름 정보 없음");
                        }
                    } else {
                        nameText.setText("⚙ 사용자 정보 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                    nameText.setText("⚙ 오류 발생");
                });
    }


    private void logoutAll(Activity activity) {
        // Firebase 로그아웃
        FirebaseAuth.getInstance().signOut();

        // Google 로그아웃 - 로그인 시와 동일한 옵션 사용
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d("Logout", "Google 로그아웃 완료");

            // 로그인 상태 초기화
            PreferenceManager.clear(activity);

            // Kakao 로그아웃
            UserApiClient.getInstance().logout(error -> {
                if (error != null) {
                    Log.e("Logout", "Kakao 로그아웃 실패", error);
                } else {
                    Log.d("Logout", "Kakao 로그아웃 성공");
                }

                Toast.makeText(activity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();



                // SplashActivity로 이동
                Intent intent = new Intent(activity, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();

                return null;
            });
        });
    }

    //paths, spots 가져와서 mergedList에 합침
    private void loadUserContent(String uid){
        if(uid == null) return;

        loadUserPaths(UserManager.getUid(this), pathList -> {
            mergedList.addAll(pathList);
            pathsLoaded = true;
            if(spotsLoaded) showMergedList();
        });
        loadUserSpots(UserManager.getUid(this), spotList -> {
            mergedList.addAll(spotList);
            spotsLoaded=true;
            if(pathsLoaded) showMergedList();
        });
    }

    //mergedList 최신순 정렬, recycler에 보여줌
    private void showMergedList() {
        // 1) mergedList를 createdAt 기준으로 내림차순 정렬
        Collections.sort(mergedList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Date d1 = (o1 instanceof PathData)
                        ? ((PathData) o1).getCreatedAt()
                        : ((SpotData) o1).createdAt;
                Date d2 = (o2 instanceof PathData)
                        ? ((PathData) o2).getCreatedAt()
                        : ((SpotData) o2).createdAt;
                return d2.compareTo(d1); // 내림차순
            }
        });

        // 2) showingAll 상태에 따라 상위 3개만 또는 전체 보여주기
        List<Object> displayList;
        if (showingAll || mergedList.size() <= 3) {
            displayList = mergedList;
        } else {
            displayList = mergedList.subList(0, 3);
        }

        // 3) RecyclerView 어댑터 세팅
        MergedAdapter adapter = new MergedAdapter(displayList, this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MergedAdapter.OnItemClickListener() {
            @Override
            public void onPathClick(PathData path) {
                mapManager.clearPath();
                spotManager.clearSpotMarker();
                moveCameraToPath(path);
                mapManager.updatePath(path.getPath());
            }
            @Override
            public void onSpotClick(SpotData spot) {
                mapManager.clearPath();
                spotManager.clearSpotMarker();
                moveCameraToSpot(spot);
                LatLng latLng = new LatLng(spot.getLatitude(),spot.getLongitude());
                spotManager.addSpotMarker(naverMap, latLng);
            }
        });

        // 4) 버튼 가시성 & 텍스트 처리
        if (mergedList.size() > 3) {
            showAllBtn.setVisibility(Button.VISIBLE);
        } else {
            showAllBtn.setVisibility(Button.GONE);
        }
    }

    private void loadUserPaths(String uid, OnPathsLoadedListener listener) {
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("paths")
                .whereEqualTo("createdBy", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<PathData> pathList = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        List<Map<String, Object>> pathPoints =
                                (List<Map<String, Object>>) document.get("path");
                        if (pathPoints != null && pathPoints.size() > 1) {
                            List<LatLng> latLngList = new ArrayList<>();
                            for (Map<String, Object> point : pathPoints) {
                                double lat = (double) point.get("lat");
                                double lng = (double) point.get("lng");
                                latLngList.add(new LatLng(lat, lng));
                            }

                            String keyword = document.getString("keyword");
                            String createdBy = document.getString("createdBy");
                            Date createdAt = document.getDate("createdAt");

                            PathData userPath = new PathData(
                                    createdBy,
                                    createdAt,
                                    keyword,
                                    latLngList
                            );
                            pathList.add(userPath);
                        }
                    }
                    listener.onPathsLoaded(pathList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "샛길 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching paths", e);
                });
    }

    private void loadUserSpots(String uid, OnSpotsLoadedListener listener) {
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("spots")
                .whereEqualTo("createdBy", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SpotData> spotList = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        double latitude = document.getDouble("latitude");
                        double longitude = document.getDouble("longitude");
                        String address = document.getString("address");
                        String keyword = document.getString("keyword");
                        String description = document.getString("description");
                        String imageUrl = document.getString("imageUrl");
                        String locationText = document.getString("locationText");
                        String createdBy = document.getString("createdBy");
                        Date createdAt = document.getDate("createdAt");

                        SpotData spot = new SpotData();
                        spot.latitude = latitude;
                        spot.longitude = longitude;
                        spot.address = address;
                        spot.keyword = keyword;
                        spot.description = description;
                        spot.imageUrl = imageUrl;
                        spot.locationText = locationText;
                        spot.createdBy = createdBy;
                        spot.createdAt = createdAt;

                        spotList.add(spot);
                    }
                    listener.onSpotsLoaded(spotList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "샛길 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching paths", e);
                });
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(naverMap -> {
            // 위치 추적 활성화
            naverMap.setLocationSource(new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE));
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

            // 현재 위치 가져오기 (FusedLocationProviderClient 사용)
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            naverMap.moveCamera(CameraUpdate.scrollTo(currentLocation));
                        } else {
                            Toast.makeText(this, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void moveCameraToSpot(SpotData spot) {
        if (naverMap == null) return;

        // SpotData에 저장된 위도/경도 꺼내오는 부분
        double latitude  = spot.latitude;   // 또는 spot.getLatitude();
        double longitude = spot.longitude;  // 또는 spot.getLongitude();

        LatLng target = new LatLng(latitude, longitude);
        CameraUpdate cu = CameraUpdate.scrollTo(target);
        naverMap.moveCamera(cu);
    }

    private void moveCameraToPath(PathData path) {
        if (naverMap == null) return;

        LatLng target = path.getPath().get(0);

        CameraUpdate cu = CameraUpdate.scrollTo(target);
        naverMap.moveCamera(cu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMapView(null); // 권한 허용 시 지도 초기화
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface OnPathsLoadedListener {
        void onPathsLoaded(List<PathData> pathList);
    }

    public interface OnSpotsLoadedListener {
        void onSpotsLoaded(List<SpotData> spotList);
    }

    // 생명주기 관리
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }


}