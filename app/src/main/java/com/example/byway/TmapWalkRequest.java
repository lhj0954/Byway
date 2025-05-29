package com.example.byway;

import com.google.gson.annotations.SerializedName;

public class TmapWalkRequest {
    @SerializedName("startX")
    public String startX;

    @SerializedName("startY")
    public String startY;

    @SerializedName("endX")
    public String endX;

    @SerializedName("endY")
    public String endY;

    @SerializedName("reqCoordType")
    public String reqCoordType = "WGS84GEO";

    @SerializedName("resCoordType")
    public String resCoordType = "WGS84GEO";

    @SerializedName("startName")
    public String startName = "출발지";

    @SerializedName("endName")
    public String endName = "도착지";

    public TmapWalkRequest(String startX, String startY, String endX, String endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
}
