package com.example.byway;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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
                name = itemView.findViewById(R.id.photo_name);
                address = itemView.findViewById(R.id.photo_address);
                image = itemView.findViewById(R.id.photo_image);
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
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.activity_photo_spot, null);

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.photo_spot_list);


        // RecyclerView 세팅
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<SpotData> list = Arrays.asList(spot); // 단일 아이템 리스트로 처리 (또는 연관 데이터 리스트)
        recyclerView.setAdapter(new SpotAdapter(context, list)); // 아래 어댑터 참조

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }




    public void clearAllSpotMarkers() {
        for (Marker marker : spotMarkers) {
            marker.setMap(null);
        }
        spotMarkers.clear();
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

