package com.example.byway.searchPOI;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KakaoApiClient {
    private static final String BASE_URL = "https://dapi.kakao.com/";
    private static final String API_KEY = "KakaoAK 3ccc6ac3d65e994e1a69242a08abeea5";
    private static KakaoApiService instance;

    public static KakaoApiService getInstance() {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Authorization", KakaoApiClient.getAuthHeader())
                                .header("KA", "sdk/1.0.0 os/android")  // os/android 혹은 origin/{도메인} 형태로 넣기
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(KakaoApiService.class);
        }
        return instance;
    }

    public static String getAuthHeader() {
        return API_KEY;
    }
}

