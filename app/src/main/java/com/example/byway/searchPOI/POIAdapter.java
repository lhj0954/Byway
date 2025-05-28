package com.example.byway.searchPOI;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.byway.MainActivity;
import com.example.byway.R;
import com.example.byway.TmapRouteManager;
import com.example.byway.UIController;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class POIAdapter extends RecyclerView.Adapter<POIAdapter.ViewHolder> {
    private List<KakaoResponse.Document> poiList;
    private Context context;
    private EditText startPoint;
    private EditText searchInput;
    private RecyclerView resultRecycler;
    private UIController uiController;
    private final OnPOIClickListener listener;

    public interface OnPOIClickListener {
        void onPOISelected(double lat, double lng, String name);
    }


    public POIAdapter(List<KakaoResponse.Document> poiList, Context context, EditText startPoint, EditText searchInput, RecyclerView resultRecycler, UIController uiController, OnPOIClickListener listener) {
        this.poiList = poiList;
        this.context = context;
        this.startPoint = startPoint;
        this.searchInput = searchInput;
        this.resultRecycler = resultRecycler;
        this.uiController = uiController;
        this.listener = listener;

    }

    // Getter 메서드
    public List<KakaoResponse.Document> getPoiList() {
        return poiList;
    }

    // Setter 메서드 (데이터 업데이트 시 UI 갱신)
    public void setPoiList(List<KakaoResponse.Document> poiList) {
        this.poiList = poiList;
        notifyDataSetChanged(); // 데이터 변경 후 RecyclerView 갱신
    }

    public void clearPoiList() {
        poiList.clear();
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KakaoResponse.Document poi = poiList.get(position);
        holder.name.setText(poi.place_name);
        holder.address.setText(poi.road_address_name != null ? poi.road_address_name : poi.address_name);

        // 후보지 선택 시
        holder.itemView.setOnClickListener(v -> {
            double lat = Double.parseDouble(poi.y);
            double lng = Double.parseDouble(poi.x);
            Toast.makeText(context, "위치: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();

            // 포커스된 EditText에 값 설정
            EditText targetEditText = startPoint.hasFocus() ? startPoint : searchInput;

// TextWatcher 제거
            TextWatcher currentWatcher = targetEditText == startPoint ?
                    uiController.getStartPointWatcher() :
                    uiController.getSearchInputWatcher();


            targetEditText.removeTextChangedListener(currentWatcher);
            targetEditText.setText(poi.place_name);
            targetEditText.addTextChangedListener(currentWatcher); // TextWatcher 재등록

            resultRecycler.setVisibility(View.GONE);

            //위도 경도 반환
            if (listener != null) {
                listener.onPOISelected(lat, lng, poi.place_name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.poi_name);
            address = itemView.findViewById(R.id.poi_address);
        }
    }
}