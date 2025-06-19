package com.example.byway;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyBottomSheetFragment extends BottomSheetDialogFragment {
	FirebaseFirestore db = FirebaseFirestore.getInstance();
	private LinearLayout examplesContainer;

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
		items.add(new CategoryItem("우리 지역 베스트 샛길", R.drawable.ic_category_best, R.color.brown));
		items.add(new CategoryItem("데이트 코스", R.drawable.ic_category_heart, R.color.heart));
		items.add(new CategoryItem("산책 코스", R.drawable.ic_category_walk, R.color.green));
		items.add(new CategoryItem("쇼핑 코스", R.drawable.ic_category_market, R.color.yellow));
		items.add(new CategoryItem("노점상", R.drawable.ic_category_fish, R.color.pink));
		items.add(new CategoryItem("사진 명소 추천 스팟", R.drawable.ic_category_camera, R.color.blue));
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
			Context context = holder.itemView.getContext();

			holder.icon.setImageResource(item.iconRes);

			// ✅ circle_background의 색상 동적 적용
			Drawable background = holder.icon.getBackground();
			if (background instanceof GradientDrawable) {
				((GradientDrawable) background.mutate()).setColor(context.getColor(item.colorRes));
			}

			holder.title.setText(item.title);

			holder.itemView.setOnClickListener(v -> {
				String title = item.title;
				Intent intent = new Intent(getContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

				switch (title) {
					case "사진 명소 추천 스팟":
						fetchAndSendSpots(context, "사진 명소", intent, NearbyBottomSheetFragment.this);
						break;
					case "노점상":
						fetchAndSendSpots(context, "노점상", intent, NearbyBottomSheetFragment.this);
						break;
					case "데이트 코스":
					case "산책 코스":
					case "쇼핑 코스":
						fetchAndSendCourses(context, title, intent,NearbyBottomSheetFragment.this);
						break;
					case "우리 지역 베스트 샛길":
						// TODO: 경로 평가 로직 필요
						break;
				}
			});
		}

		private void fetchAndSendSpots(Context context, String keyword, Intent intent, NearbyBottomSheetFragment fragment) {
			db.collection("spots")
					.whereEqualTo("keyword", keyword)
					.get()
					.addOnSuccessListener(queryDocumentSnapshots -> {
						ArrayList<SpotData> spotList = new ArrayList<>();
						for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
							SpotData spot = doc.toObject(SpotData.class);
							if (spot != null) {
								spot.id = doc.getId();
								spotList.add(spot);
							}
						}

						intent.putExtra("spots", spotList);
						context.startActivity(intent);
						System.out.println(spotList.size());

						fragment.dismiss();
					})
					.addOnFailureListener(e -> {
						Toast.makeText(getContext(), "데이터 로드 실패", Toast.LENGTH_SHORT).show();
					});
		}

		private void fetchAndSendCourses(Context context, String category, Intent intent, NearbyBottomSheetFragment fragment) {
			db.collection("paths")
					.whereEqualTo("keyword", category)
					.get()
					.addOnSuccessListener(querySnapshot -> {
						// 샛길마다 좌표 리스트를 담을 상위 리스트
						List<List<LatLng>> allCoords = new ArrayList<>();
						for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
							List<Map<String, Object>> rawPath = (List<Map<String, Object>>) doc.get("path");
							if (rawPath == null) continue;

							List<LatLng> pathCoords = new ArrayList<>();
							for (Map<String, Object> p : rawPath) {
								double lat = ((Number) p.get("lat")).doubleValue();
								double lng = ((Number) p.get("lng")).doubleValue();
								pathCoords.add(new LatLng(lat, lng));
							}
							allCoords.add(pathCoords);
						}

						FragmentActivity fa = fragment.getActivity();
						if (fa instanceof MainActivity) {
							MainActivity main = (MainActivity) fa;
							main.runOnUiThread(() -> {
								// allCoords 대신 yourBywayPaths 리스트 이름을 사용하세요
								List<List<LatLng>> yourBywayPaths = allCoords;

								// TmapRouteManager 의 한 번에 그리기 메서드 호출
								main.drawCategoryPath(yourBywayPaths);
							});
						} else {
							Log.e("NearbyAdapter", "context is not MainActivity!");
						}

						fragment.dismiss();
					})
					.addOnFailureListener(e -> {
						Toast.makeText(getContext(), "코스 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
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
