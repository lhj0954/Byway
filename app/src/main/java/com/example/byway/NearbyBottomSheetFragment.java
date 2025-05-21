package com.example.byway;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class NearbyBottomSheetFragment extends BottomSheetDialogFragment {

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
		items.add(new CategoryItem("우리 지역 베스트 샛길", "156", R.drawable.ic_launcher_foreground, R.color.brown));
		items.add(new CategoryItem("데이트 코스", "156", R.drawable.ic_launcher_foreground, R.color.heart));
		items.add(new CategoryItem("산책 코스", "156", R.drawable.ic_launcher_foreground, R.color.green));
		items.add(new CategoryItem("쇼핑 코스", "156", R.drawable.ic_launcher_foreground, R.color.yellow));
		items.add(new CategoryItem("붕어빵 & 노점상", "156", R.drawable.ic_launcher_foreground, R.color.pink));
		items.add(new CategoryItem("사진 명소 추천 스팟", "156", R.drawable.ic_launcher_foreground, R.color.blue));
		return items;
	}

	public static class CategoryItem {
		public final String title;
		public final String count;
		public final int iconRes;
		public final int colorRes;

		public CategoryItem(String title, String count, int iconRes, int colorRes) {
			this.title = title;
			this.count = count;
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
			holder.count.setText(item.count);

			holder.itemView.setOnClickListener(v -> {
				if ("사진 명소 추천 스팟".equals(item.title)) {
					Intent intent = new Intent(getContext(), PhotoSpotActivity.class);
					startActivity(intent);
				}
			});
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			ImageView icon;
			TextView title;
			TextView count;

			ViewHolder(@NonNull View itemView) {
				super(itemView);
				icon = itemView.findViewById(R.id.icon);
				title = itemView.findViewById(R.id.title);
				count = itemView.findViewById(R.id.count);
			}
		}
	}
}
