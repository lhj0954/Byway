package com.example.byway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TmapApiService {
    @POST("routes/pedestrian?version=1")
    Call<TmapResponse> getWalkingRoute(
            @Header("appKey") String appKey,
            @Body TmapWalkRequest request
    );
}
