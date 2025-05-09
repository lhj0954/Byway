package com.example.byway;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Header;

public interface NaverDirectionApi {
    @POST("/map-direction/v1/driving")
    Call<WalkRouteResponse> getWalkRoute(
        @Body WalkRouteRequest request,
        @Header("X-NCP-APIGW-API-KEY-ID") String clientId,
        @Header("X-NCP-APIGW-API-KEY") String clientSecret
    );
}
