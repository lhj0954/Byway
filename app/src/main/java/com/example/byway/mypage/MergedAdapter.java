package com.example.byway.mypage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.byway.R;
import com.example.byway.SpotData;
import com.example.byway.PathData;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MergedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_PATH = 0;
    private static final int TYPE_SPOT = 1;

    private final List<Object> items;
    private final Context context;
    private OnItemClickListener listener;

    public MergedAdapter(List<Object> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof PathData) {
            return TYPE_PATH;
        } else {
            return TYPE_SPOT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_PATH) {
            View view = inflater.inflate(R.layout.item_user_path, parent, false);
            return new PathViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_user_spot, parent, false);
            return new SpotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof PathViewHolder) {
            ((PathViewHolder) holder).bind((PathData) item);

            //MainPageActivity에서 콜백 받아서 처리해주면 됨
            holder.itemView.setOnClickListener(v->{
                if(listener != null) listener.onPathClick((PathData) item);
            });
        } else {
            ((SpotViewHolder) holder).bind((SpotData) item);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSpotClick((SpotData) item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PathViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, keywordText, timestampText;

        public PathViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.path_title);
            keywordText = itemView.findViewById(R.id.path_keyword);
            timestampText = itemView.findViewById(R.id.path_timestamp);
        }

        void bind(PathData path) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);
            String formattedDate = sdf.format(path.getCreatedAt());

            titleText.setText("샛길");  // 항상 "샛길" 고정
            keywordText.setText("#" + path.getKeyword());
            timestampText.setText("#"+formattedDate);
        }
    }

    public interface OnItemClickListener {
        void onPathClick(PathData path);
        void onSpotClick(SpotData spot);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }


    static class SpotViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, keywordText, descriptionText, addressText, timestampText;

        public SpotViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.spot_title);
            keywordText = itemView.findViewById(R.id.spot_keyword);
            descriptionText = itemView.findViewById(R.id.spot_description);
            addressText = itemView.findViewById(R.id.spot_address);
            timestampText = itemView.findViewById(R.id.spot_timestamp);
        }

        void bind(SpotData spot) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);
            String formattedDate = sdf.format(spot.createdAt);

            titleText.setText("스팟");
            keywordText.setText("#" + spot.keyword);
            descriptionText.setText("#"+spot.description);
            addressText.setText("#"+spot.address);
            timestampText.setText("#"+formattedDate);
        }
    }
}