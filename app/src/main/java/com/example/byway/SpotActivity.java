package com.example.byway;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SpotActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView spotLocationTextView;
    private Spinner keywordSpinner;
    private EditText descriptionEditText;
    private ImageView spotImageView;
    private Button selectImageButton;
    private Button completeButton; // 등록 완료
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
        //spotLocationTextView.setText("위도: " + latitude + "\n경도: " + longitude);

        // 드롭다운 설정
        String[] keywords = {"노점상", "위험구역"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, keywords);
        keywordSpinner.setAdapter(adapter);

        //갤러리 사진 선택
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
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
            Toast.makeText(this, "스팟이 등록되었습니다!", Toast.LENGTH_SHORT).show();

            // 처음 화면(MainActivity)으로 이동 + 백스택 제거
            Intent intent = new Intent(SpotActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // SpotActivity 종료
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
