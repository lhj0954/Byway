package com.example.byway.searchPOI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoApiService {
    @GET("v2/local/search/keyword.json")
    Call<KakaoResponse> searchPlaces(
            @Header("Authorization") String auth,
            @Query("query") String query,
            @Query("y") double latitude,
            @Query("x") double longitude,
            @Query("radius") int radius
    );
}

