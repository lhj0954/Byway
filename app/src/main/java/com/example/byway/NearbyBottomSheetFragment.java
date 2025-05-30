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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NearbyBottomSheetFragment extends BottomSheetDialogFragment {
	FirebaseFirestore db = FirebaseFirestore.getInstance();
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nearby_bottom_sheet, container, false);

		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(new NearbyAdapter(getMockData()));

		return view;
	}

	private List<CategoryItem> getMockData() {
		List<CategoryItem> items = new ArrayList<>();
		items.add(new CategoryItem("우리 지역 베스트 샛길",  R.drawable.ic_launcher_foreground, R.color.brown));
		items.add(new CategoryItem("데이트 코스",  R.drawable.ic_launcher_foreground, R.color.heart));
		items.add(new CategoryItem("산책 코스",  R.drawable.ic_launcher_foreground, R.color.green));
		items.add(new CategoryItem("쇼핑 코스",  R.drawable.ic_launcher_foreground, R.color.yellow));
		items.add(new CategoryItem("노점상",  R.drawable.ic_launcher_foreground, R.color.pink));
		items.add(new CategoryItem("사진 명소 추천 스팟", R.drawable.ic_launcher_foreground, R.color.blue));
		return items;
	}

	public static class CategoryItem {
		public final String title;
		public final int iconRes;
		public final int colorRes;

		public CategoryItem(String title, int iconRes, int colorRes) {
			this.title = title;
			this.iconRes = iconRes;
			this.colorRes = colorRes;
		}
	}

	public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.ViewHolder> {
		private final List<CategoryItem> items;

		public NearbyAdapter(List<CategoryItem> items) {
			this.items = items;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			CategoryItem item = items.get(position);
			holder.icon.setImageResource(item.iconRes);
			holder.icon.setColorFilter(holder.itemView.getContext().getColor(item.colorRes));
			holder.title.setText(item.title);


			holder.itemView.setOnClickListener(v -> {
				String title = item.title;
				Context context = holder.itemView.getContext();
				Intent intent = new Intent(getContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

				switch (title) {
					case "사진 명소 추천 스팟":
						fetchAndSendSpots(context,"사진 명소", intent);
						break;
					case "노점상":
						fetchAndSendSpots(context,"노점상", intent);
						break;
					case "데이트 코스":
					case "산책 코스":
						// TODO: 산책/데이트 코스 처리
						break;
				}
				// ✅ 이 프래그먼트를 직접 닫기
				NearbyBottomSheetFragment.this.dismiss();
			});
		}

		private void fetchAndSendSpots(Context context, String keyword, Intent intent) {
			db.collection("spots")
					.whereEqualTo("keyword", keyword)
					.get()
					.addOnSuccessListener(queryDocumentSnapshots -> {
						ArrayList<SpotData> spotList = new ArrayList<>();
						for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
							SpotData spot = doc.toObject(SpotData.class);
							if (spot != null) {
								spotList.add(spot);
							}
						}

						intent.putExtra("spots", spotList);
						context.startActivity(intent);
						System.out.println(spotList.size());
					})
					.addOnFailureListener(e -> {
						Toast.makeText(getContext(), "데이터 로드 실패", Toast.LENGTH_SHORT).show();
					});
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			ImageView icon;
			TextView title;

			ViewHolder(@NonNull View itemView) {
				super(itemView);
				icon = itemView.findViewById(R.id.icon);
				title = itemView.findViewById(R.id.title);
			}
		}
	}
}
