package com.example.byway;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

public class SpotActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView spotLocationTextView;
    private Spinner keywordSpinner;
    private EditText descriptionEditText;
    private ImageView spotImageView;
    private Button selectImageButton;
    private Button completeButton; // 등록 완료
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);

        // Toolbar 연결
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 왼쪽 상단 뒤로가기 버튼 표시
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // 뷰 연결
        spotLocationTextView = findViewById(R.id.spotLocationTextView);
        keywordSpinner = findViewById(R.id.keywordSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        spotImageView = findViewById(R.id.spotImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        completeButton = findViewById(R.id.completeButton);

        // 좌표 전달받기
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        // 주소 변환 및 표시
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                spotLocationTextView.setText("위치: " + address);
            } else {
                spotLocationTextView.setText("주소를 찾을 수 없습니다");
            }
        } catch (Exception e) {
            e.printStackTrace();
            spotLocationTextView.setText("주소 변환 실패");
        }

        // 드롭다운 설정
        String[] keywords = {"노점상", "사진 명소"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, keywords);
        keywordSpinner.setAdapter(adapter);

        //갤러리 사진 선택
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            spotImageView.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // 사진 선택 버튼
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });


        //등록 완료
        completeButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                saveSpotToFirestore(selectedImageUri);
            } else {
                Toast.makeText(this, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show();
            }

            // 처음 화면(MainActivity)으로 이동 + 백스택 제거
            Intent intent = new Intent(SpotActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // SpotActivity 종료
        });
    }

    private void saveSpotToFirestore(Uri imageUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String uid = UserManager.getUid(this);
        if (uid == null) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
            Log.e("SpotUploader", "UID is null!");
            return;
        }

        // 파일 이름 생성 (중복 방지용)
        String fileName = "spot_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);

        // 이미지 업로드
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 업로드 완료 후 다운로드 URL 가져오기
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // SpotData 생성 및 Firestore 저장
                        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
                        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
                        String address = spotLocationTextView.getText().toString();
                        String keyword = keywordSpinner.getSelectedItem().toString();
                        String description = descriptionEditText.getText().toString();
                        String locationText = spotLocationTextView.getText().toString();

                        SpotData spot = new SpotData(latitude, longitude, address, keyword, description, downloadUrl, locationText, uid);

                        db.collection("spots")
                                .add(spot)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "스팟이 등록되었습니다!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SpotActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "DB 저장 실패", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                });
    }



    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SpotActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("restoreSpotMode", true); // 되돌아가서 스팟 등록 모드 유지
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
