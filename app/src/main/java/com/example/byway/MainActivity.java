package com.example.byway;

import android.Manifest;
import android.content.Intent;
import android.content.pm.Signature;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

//경로추천 import
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDelegate; // ✅ 추가됨
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
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
    private boolean isRecording = false; //길 등록중인지
    private boolean isAddingSpot = false; //스팟 등록중인지
    private boolean isFabOpen = false;
    private boolean isStartPointInitialized = false;
    private boolean isViewingSpots =false;
    private PathRecorder pathRecorder;
    private MapManager mapManager;
    private SpotManager spotManager;
    private Button finishRecordButton, submitPathButton, addSpotButton, cancelSpotButton, selectSpotButton, searchButton ;
    private LinearLayout recordingControls;
    private LinearLayout fabSubContainer;
    private LatLng selectedSpot; //스팟위치
    private Marker spotMarker; //스팟 마커
    private ActivityResultLauncher<Intent> spotActivityLauncher;

    private FloatingActionButton fabSubLeft, fabSubRight;
    private EditText startPoint, searchInput;

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isAddingSpot() {
        return isAddingSpot;
    }

    public void setAddingSpot(boolean addingSpot) {
        isAddingSpot = addingSpot;
    }

    public PathRecorder getPathRecorder() {
        return pathRecorder;
    }


    public void setPathRecorder(PathRecorder pathRecorder) {
        this.pathRecorder = pathRecorder;
    }

    public Button getFinishRecordButton() {
        return finishRecordButton;
    }

    public void setFinishRecordButton(Button finishRecordButton) {
        this.finishRecordButton = finishRecordButton;
    }

    public Button getSubmitPathButton() {
        return submitPathButton;
    }

    public void setSubmitPathButton(Button submitPathButton) {
        this.submitPathButton = submitPathButton;
    }

    public Button getAddSpotButton() {
        return addSpotButton;
    }

    public void setAddSpotButton(Button addSpotButton) {
        this.addSpotButton = addSpotButton;
    }

    public Button getCancelSpotButton() {
        return cancelSpotButton;
    }

    public void setCancelSpotButton(Button cancelSpotButton) {
        this.cancelSpotButton = cancelSpotButton;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public void setSearchButton(Button searchButton) {
        this.searchButton = searchButton;
    }

    public void setSelectSpotButton(Button selectSpotButton) {
        this.selectSpotButton = selectSpotButton;
    }

    public LinearLayout getRecordingControls() {
        return recordingControls;
    }

    public void setRecordingControls(LinearLayout recordingControls) {
        this.recordingControls = recordingControls;
    }
    
    public LatLng getSelectedSpot() {
        return selectedSpot;
    }

    public void setSelectedSpot(LatLng selectedSpot) {
        this.selectedSpot = selectedSpot;
    }

    public ActivityResultLauncher<Intent> getSpotActivityLauncher() {
        return spotActivityLauncher;
    }
    public boolean isFabOpen() {
        return isFabOpen;
    }
    public void setFabOpen(boolean fabOpen) {
        isFabOpen = fabOpen;
    }

    public boolean isViewingSpots() {
        return isViewingSpots;
    }

    public void setViewingSpots(boolean viewingSpots) {
        isViewingSpots = viewingSpots;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); //onMapReady 호출

        // 위치 소스 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        pathRecorder=new PathRecorder();

        //스팟등록모드
        spotActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("restoreSpotMode", false)) {
                            isAddingSpot = true;
                            cancelSpotButton.setVisibility(View.VISIBLE);
                            selectSpotButton.setVisibility(View.VISIBLE);
                            addSpotButton.setVisibility(View.GONE);
                            Toast.makeText(this, "스팟 등록 모드로 돌아왔습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        mapManager = new MapManager(naverMap);
        spotManager = new SpotManager();

        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setLocationButtonEnabled(false);

        UIController uiController = new UIController(this, naverMap, mapManager, spotManager);
        uiController.setupUI();
        uiController.setupMapListeners();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        naverMap.addOnLocationChangeListener(location -> {
            if (location != null) {
                Location loc = new Location("");
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                setLastLocation(loc);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 전달받은 사진 명소 스팟 데이터 처리
        if (intent != null && intent.hasExtra("spots")) {
            ArrayList<SpotData> photoSpots = (ArrayList<SpotData>) intent.getSerializableExtra("spots");

            spotManager.showRecommendedSpots(naverMap, photoSpots);
            setViewingSpots(true);
        }
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