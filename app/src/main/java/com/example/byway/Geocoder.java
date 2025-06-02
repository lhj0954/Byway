package com.example.byway;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Geocoder {

    private static final String BASE_URL = "https://apis.openapi.sk.com/";
    private static final String APP_KEY = "iwCg3TsBeQ35D1YwoVsYm9yaP1NT6UtW3vXIRpEK"; // 본인의 키로 대체

    private final TmapGeocodingService geocodingService;
    private final Context context;

    public Geocoder(Context context) {
        this.context = context;

        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geocodingService = retrofit.create(TmapGeocodingService.class);
    }

    // 콜백 인터페이스 정의
    public interface GeocodeCallback {
        void onResult(double lat, double lon);
    }

    // 실제 주소 변환 메서드
    public void geocodeAddress(String address, GeocodeCallback callback) {
        Call<TmapGeocodeResponse> call = geocodingService.getCoordinates(
                "1",
                address,
                APP_KEY
        );

        call.enqueue(new Callback<TmapGeocodeResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmapGeocodeResponse> call, @NonNull Response<TmapGeocodeResponse> response) {
                //Log.d("Response", "" + response.isSuccessful()); -> true
                //Log.d("Response", "" + response.body()); -> com.example.byway.TmapGeocodeResponse@4023c77
                //Log.d("Response", ""+ response.body().addressInfo.coordinateInfo);
                Log.d("GEO_RAW", response.raw().toString());
                Log.d("GEO_BODY", new Gson().toJson(response.body()));

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().coordinateInfo != null
                        && response.body().coordinateInfo.coordinate != null
                        && !response.body().coordinateInfo.coordinate.isEmpty())
                {
                    TmapGeocodeResponse.Coordinate coordinateInfo = response.body().coordinateInfo.coordinate.get(0);
                    double lat = Double.parseDouble(coordinateInfo.newLat);
                    double lon = Double.parseDouble(coordinateInfo.newLon);
                    callback.onResult(lat, lon);
                } else {
                    Toast.makeText(context, "주소 변환 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TmapGeocodeResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "주소 요청 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
