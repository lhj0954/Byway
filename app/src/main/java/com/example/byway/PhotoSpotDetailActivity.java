package com.example.byway;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhotoSpotDetailActivity extends AppCompatActivity {

    private ImageView emojiHappy, emojiNeutral, emojiSad;
    private String selectedEmotion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_spot_detail);

        // 장소 정보
        TextView name = findViewById(R.id.detail_name);
        TextView address = findViewById(R.id.detail_address);
        ImageView image = findViewById(R.id.detail_image);

        name.setText(getIntent().getStringExtra("name"));
        address.setText(getIntent().getStringExtra("address"));
        image.setImageResource(getIntent().getIntExtra("imageRes", 0));

        // View 바인딩
        Button writeCommentBtn = findViewById(R.id.write_comment_button);
        LinearLayout commentSection = findViewById(R.id.comment_section);
        LinearLayout commentExamples = findViewById(R.id.comment_examples);
        EditText commentInput = findViewById(R.id.comment_input);
        Button commentSubmitBtn = findViewById(R.id.comment_submit_button);
        Button attachPhotoBtn = findViewById(R.id.photo_attach_button);

        emojiHappy = findViewById(R.id.emoji_happy);
        emojiNeutral = findViewById(R.id.emoji_neutral);
        emojiSad = findViewById(R.id.emoji_sad);

        // 작성 버튼 클릭 → 예시 숨기고 작성창 보이기
        writeCommentBtn.setOnClickListener(v -> {
            commentExamples.setVisibility(View.GONE);
            commentSection.setVisibility(View.VISIBLE);
        });

        // 이모지 선택
        emojiHappy.setOnClickListener(v -> selectEmotion("happy"));
        emojiNeutral.setOnClickListener(v -> selectEmotion("neutral"));
        emojiSad.setOnClickListener(v -> selectEmotion("sad"));

        // 등록 버튼 클릭 → 작성창 숨기고 예시 다시 보이기
        commentSubmitBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "코멘트를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "감정: " + selectedEmotion + "\n내용: " + commentText, Toast.LENGTH_LONG).show();

            // 입력창 초기화 및 감정 선택 초기화
            commentInput.setText("");
            selectedEmotion = "";
            emojiHappy.setAlpha(0.3f);
            emojiNeutral.setAlpha(0.3f);
            emojiSad.setAlpha(0.3f);

            // 작성창 닫기, 예시 다시 보이기
            commentSection.setVisibility(View.GONE);
            commentExamples.setVisibility(View.VISIBLE);
        });

        // 사진 첨부 버튼
        attachPhotoBtn.setOnClickListener(v -> {
            Toast.makeText(this, "사진 첨부는 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectEmotion(String emotion) {
        selectedEmotion = emotion;
        emojiHappy.setAlpha(1f);
        emojiNeutral.setAlpha(0.3f);
        emojiSad.setAlpha(0.3f);

        switch (emotion) {
            case "happy":
                emojiHappy.setAlpha(1f);
                break;
            case "neutral":
                emojiNeutral.setAlpha(1f);
                break;
            case "sad":
                emojiSad.setAlpha(1f);
                break;
        }
    }
}
