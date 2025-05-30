package com.example.byway;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoSpotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_spot);

        TextView titleText = findViewById(R.id.photo_spot_title);
        TextView countText = findViewById(R.id.photo_spot_count);
        RecyclerView recyclerView = findViewById(R.id.photo_spot_list);

        titleText.setText("사진 명소 추천 스팟");
        countText.setText("156");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<PhotoSpot> spots = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    static class PhotoSpot {
        String name, address, imageUrl;

        PhotoSpot(String name, String address, String imageUrl) {
            this.name = name;
            this.address = address;
            this.imageUrl = imageUrl;
        }
    }

    static class PhotoSpotAdapter extends RecyclerView.Adapter<PhotoSpotAdapter.ViewHolder> {
        List<PhotoSpot> list;
        Context context;

        PhotoSpotAdapter(Context context, List<PhotoSpot> list) {
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
            PhotoSpot spot = list.get(position);
            holder.name.setText(spot.name);
            holder.address.setText(spot.address);
            Glide.with(context)
                    .load(spot.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // 대체 이미지
                    .into(holder.image);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhotoSpotDetailActivity.class);
                intent.putExtra("name", spot.name);
                intent.putExtra("address", spot.address);
                intent.putExtra("imageUrl", spot.imageUrl);
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
}