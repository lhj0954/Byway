package com.example.byway;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TmapGeocodeResponse {
    public CoordinateInfo coordinateInfo;

    public static class CoordinateInfo {
        public String coordType;
        public String addressFlag;
        public String page;
        public String count;
        public String totalCount;
        public List<Coordinate> coordinate;
    }

    public static class Coordinate {
        public String newLat;
        public String newLon;
        public String city_do;
        public String gu_gun;
        public String buildingName;
        public String newRoadName;
        public String newBuildingIndex;
    }
}
