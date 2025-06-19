package com.example.byway;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TmapGeocodingService {
    @GET("tmap/geo/fullAddrGeo")
    Call<TmapGeocodeResponse> getCoordinates(
            @Query("version") String version,
            @Query("fullAddr") String fullAddr,
            @Query("appKey") String appKey // 여기로 옮김
    );
}
