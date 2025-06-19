package com. example. byway. searchPOI;

import android.app.Activity;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.example.byway.MainActivity;
import com.example.byway.UIController;
import com.example.byway.searchPOI.KakaoApiClient;
import com.example.byway.searchPOI.KakaoApiService;
import com.example.byway.searchPOI.KakaoResponse;
import com.example.byway.searchPOI.POIAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTextWatcher implements TextWatcher {
    private final Activity activity;
    private final POIAdapter adapter;
    private final RecyclerView resultRecycler;
    private String latestKeyword = "";
    private final boolean isStartPoint;
    private final UIController uiController;


    public SearchTextWatcher(Activity activity, POIAdapter adapter, RecyclerView resultRecycler, boolean isStartPoint, UIController uiController) {
        this.activity = activity;
        this.adapter = adapter;
        this.resultRecycler = resultRecycler;
        this.isStartPoint = isStartPoint;
        this.uiController = uiController;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s == null || s.length() == 0) {
            latestKeyword = "";
            adapter.setPoiList(new ArrayList<>());
            resultRecycler.setVisibility(View.GONE);
            return;
        }

        //주소검색 때문에
        if (uiController != null) {
            if (isStartPoint) {
                uiController.setStartPointLat(null);
                uiController.setStartPointLng(null);
            } else {
                uiController.setSearchInputLat(null);
                uiController.setSearchInputLng(null);
            }
        }

        latestKeyword = s.toString();
        searchPlaces(latestKeyword);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    private void searchPlaces(String keyword) {
        double lat = 37.5665, lng = 126.9780;

        Location location = ((MainActivity) activity).getLastLocation(); // 반드시 getLastLocation 메서드가 public이어야 함
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }

        KakaoApiService apiService = KakaoApiClient.getInstance();
        apiService.searchPlaces(KakaoApiClient.getAuthHeader(), keyword, lat, lng, 5000)
                .enqueue(new Callback<KakaoResponse>() {
                    @Override
                    public void onResponse(Call<KakaoResponse> call, Response<KakaoResponse> response) {
                        if (!keyword.equals(latestKeyword)) return;

                        List<KakaoResponse.Document> list = response.body() != null ? response.body().documents : null;
                        if (list != null && !list.isEmpty()) {
                            adapter.setPoiList(list);
                            resultRecycler.setVisibility(View.VISIBLE);
                        } else {
                            adapter.setPoiList(new ArrayList<>());
                            resultRecycler.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<KakaoResponse> call, Throwable t) {
                        if (!keyword.equals(latestKeyword)) return;
                        Log.e("SearchDebug", "API 오류: " + t.getMessage());
                    }
                });
    }
}
