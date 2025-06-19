package com.example.byway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

//마커 관리만
public class SpotManager {
    private Marker spotMarker;
    private LatLng selectedSpot;
    private final List<Marker> spotMarkers = new ArrayList<>();
    public static class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.ViewHolder> {
        List<SpotData> list;
        Context context;

        public SpotAdapter(Context context, List<SpotData> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_photo_spot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SpotData spot = list.get(position);
            holder.name.setText(spot.getDescription());
            holder.address.setText(spot.getAddress());
            Glide.with(context)
                    .load(spot.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.image);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhotoSpotDetailActivity.class);
                intent.putExtra("name", spot.getDescription());
                intent.putExtra("address", spot.getAddress());
                intent.putExtra("imageUrl", spot.getImageUrl());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, address;
            ImageView image;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.detail_name);
                address = itemView.findViewById(R.id.detail_address);
                image = itemView.findViewById(R.id.detail_image);
            }
        }
    }


    //스팟 보여줄때
    public void showRecommendedSpots(NaverMap naverMap, List<SpotData> spots) {
        clearAllSpotMarkers(); // 기존 마커 제거

        for (SpotData spot : spots) {
            Marker marker = new Marker();
            marker.setPosition(new LatLng(spot.getLatitude(), spot.getLongitude()));
            marker.setMap(naverMap);

            marker.setOnClickListener(overlay -> {
                showSpotBottomSheet(naverMap.getContext(), spot);

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(spot.getLatitude(),spot.getLongitude())).animate(CameraAnimation.Easing);
                naverMap.moveCamera(cameraUpdate);
                return true; // 클릭 이벤트 소비함
            });

            spotMarkers.add(marker);
        }
    }

    public void showSpotBottomSheet(Context context, SpotData spot) {
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.activity_photo_spot_detail, null);

        // ★ 클릭 소비용 처리 추가
        bottomSheetView.setClickable(true);
        bottomSheetView.setOnClickListener(v -> {
            // do nothing
        });
        TextView nameView    = bottomSheetView.findViewById(R.id.detail_name);
        TextView addressView = bottomSheetView.findViewById(R.id.detail_address);
        ImageView imageView  = bottomSheetView.findViewById(R.id.detail_image);
        Button commentBtn    = bottomSheetView.findViewById(R.id.write_comment_button);
        LinearLayout commentSection = bottomSheetView.findViewById(R.id.comment_section);
        Button submitCommentBtn = bottomSheetView.findViewById(R.id.comment_submit_button);
        Button navigateBtn = bottomSheetView.findViewById(R.id.navigate_button);
        EditText commentInput = bottomSheetView.findViewById(R.id.comment_input);
        ImageView emojiHappy = bottomSheetView.findViewById(R.id.emoji_happy);
        ImageView emojiNeutral = bottomSheetView.findViewById(R.id.emoji_neutral);
        ImageView emojiSad = bottomSheetView.findViewById(R.id.emoji_sad);

        final String[] selectedEmoji = {""};

        // 4) 데이터를 뷰에 세팅
        nameView.setText(spot.locationText);
        addressView.setText(spot.address);
        // Glide 또는 Picasso 로 이미지 로딩
        Glide.with(context)
                .load(spot.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imageView);

        RecyclerView commentListView = bottomSheetView.findViewById(R.id.comment_list);
        List<CommentData> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(context, commentList, spot.getId());
        commentListView.setLayoutManager(new LinearLayoutManager(context));
        commentListView.setAdapter(commentAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("spots").document(spot.getId())
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String userName = doc.getString("userName");
                        String content = doc.getString("content");
                        String emoji = doc.getString("emoji");
                        String commentId = doc.getId();
                        String userId = doc.getString("userId");

                        commentList.add(new CommentData(userName, content, emoji, commentId, spot.getId(), userId));
                    }
                    commentAdapter.notifyDataSetChanged();

                });

        // 5) 버튼 클릭 리스너
        commentBtn.setOnClickListener(v -> {
            // 5-1) 로그인 체크 예시
            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (PreferenceManager.isUserLoggedIn(context)) {
                // 로그인 돼 있으면 코멘트 입력 섹션 보이기
                commentSection.setVisibility(View.VISIBLE);
            } else {
                // 로그인 유도
                new AlertDialog.Builder(context)
                        .setTitle("로그인이 필요합니다")
                        .setMessage("코멘트를 작성하려면 로그인이 필요해요.")
                        .setPositiveButton("로그인", (d, w) ->
                                context.startActivity(new Intent(context, LoginActivity.class)))
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        navigateBtn.setOnClickListener(v -> {
            LatLng destination = new LatLng(spot.getLatitude(), spot.getLongitude());

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);
                locationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double startLat = location.getLatitude();
                        double startLng = location.getLongitude();
                        double destLat = destination.latitude;
                        double destLng = destination.longitude;

                        // MainActivity로 캐스팅 (context가 MainActivity인 경우만 유효)
                        if (context instanceof MainActivity) {
                            MainActivity activity = (MainActivity) context;
                            TmapRouteManager routeManager = new TmapRouteManager(activity, activity.getNaverMap());
                            routeManager.walkToSpot(startLng, startLat, destLng, destLat);
                        } else {
                            Toast.makeText(context, "경로 안내를 지원하지 않는 화면입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1001
                );
            }
        });

        submitCommentBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();

            if (commentText.isEmpty()) {
                Toast.makeText(context, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }


            String un = "";
            long userId = 0;
            // 현재 로그인한 사용자 정보 가져오기
            UserApiClient.getInstance().me((user, thowable) -> {
                if (user != null) {
                    long kakaoId = user.getId();
                    String uid = String.valueOf(kakaoId);

                    //FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    String userName = document.getString("userName");

                                    // ✅ 댓글 작성 시 사용할 정보: uid, userName
                                    postComment(uid, userName, commentText, selectedEmoji[0],commentInput, context, spot);
                                    commentSection.setVisibility(View.GONE);
                                }
                            });
                }
                return null;
            });
        });

        emojiHappy.setOnClickListener(v -> {
            selectedEmoji[0] = "happy";
            highlightSelectedEmoji(emojiHappy, emojiNeutral, emojiSad);
        });

        emojiNeutral.setOnClickListener(v -> {
            selectedEmoji[0] = "neutral";
            highlightSelectedEmoji(emojiNeutral, emojiHappy, emojiSad);
        });

        emojiSad.setOnClickListener(v -> {
            selectedEmoji[0] = "sad";
            highlightSelectedEmoji(emojiSad, emojiHappy, emojiNeutral);
        });

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(bottomSheetView);
        dialog.show();

    }


    private void postComment(String userId, String userName, String commentText, String emoji, EditText commentInput, Context context, SpotData spot) {
        if (commentText.isEmpty()) {
            Toast.makeText(context, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", userId);
        commentData.put("userName", userName);
        commentData.put("content", commentText);
        commentData.put("timestamp", FieldValue.serverTimestamp());
        commentData.put("emoji", emoji);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("spots").document(spot.getId()) // 해당 스팟 문서 ID 사용
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "댓글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    public void clearAllSpotMarkers() {
        for (Marker marker : spotMarkers) {
            marker.setMap(null);
        }
        spotMarkers.clear();
    }

    private void highlightSelectedEmoji(ImageView selected, ImageView... others) {
        selected.setAlpha(1.0f); // 강조
        for (ImageView other : others) {
            other.setAlpha(0.3f); // 흐리게
        }
    }

    //스팟 등록할때
    public void addSpotMarker(NaverMap naverMap, LatLng spot) {
        selectedSpot = spot;
        //기존에 마커있으면 제거
        if (spotMarker != null) {
            spotMarker.setMap(null);
        }
        spotMarker = new Marker();
        spotMarker.setPosition(selectedSpot);
        spotMarker.setMap(naverMap);
    }

    public void clearSpotMarker() {
        if (spotMarker != null) {
            spotMarker.setMap(null);
            spotMarker = null;
        }
        selectedSpot = null;
    }

    public LatLng getSelectedSpot() {
        return selectedSpot;
    }
}

