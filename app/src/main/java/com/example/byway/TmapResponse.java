package com.example.byway;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TmapResponse {
    @SerializedName("features")
    public List<Feature> features;

    public static class Feature {
        @SerializedName("type")
        public String type;

        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("properties")
        public Properties properties;
    }

    public static class Geometry {
        @SerializedName("type")
        public String type;

        // coordinates는 Point면 List<Double>, LineString이면 List<List<Double>>
        @SerializedName("coordinates")
        public Object coordinates;
    }

    public static class Properties {
        @SerializedName("index")
        public int index;

        @SerializedName("pointIndex")
        public int pointIndex;

        @SerializedName("lineIndex")
        public int lineIndex;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("turnType")
        public int turnType;

        @SerializedName("pointType")
        public String pointType;

        @SerializedName("distance")
        public int distance;

        @SerializedName("time")
        public int time;
    }
}
