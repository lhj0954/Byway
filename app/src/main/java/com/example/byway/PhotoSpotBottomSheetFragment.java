package com.example.byway;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoSpotBottomSheetFragment extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private TextView countText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_bottom_sheet, container, false);

        recyclerView = view.findViewById(R.id.photo_spot_list);
        countText = view.findViewById(R.id.photo_spot_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadPhotoSpots();

        return view;
    }

    private void loadPhotoSpots() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<PhotoSpotActivity.PhotoSpot> spots = new ArrayList<>();

        db.collection("spots")
                .whereEqualTo("keyword", "사진 명소")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("description");
                        String address = doc.getString("address");
                        String imageUrl = doc.getString("imageUrl");

                        spots.add(new PhotoSpotActivity.PhotoSpot(name, address, imageUrl));
                    }

                    PhotoSpotActivity.PhotoSpotAdapter adapter = new PhotoSpotActivity.PhotoSpotAdapter(requireContext(), spots);
                    recyclerView.setAdapter(adapter);

                    countText.setText(String.valueOf(spots.size()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터 로드 실패", Toast.LENGTH_SHORT).show();
                });
    }
}

