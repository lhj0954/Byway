package com.example.byway.searchPOI;

import java.util.List;

public class KakaoResponse {
    public List<Document> documents;

    public static class Document {
        public String place_name;
        public String address_name;
        public String road_address_name;
        public String x; // longitude
        public String y; // latitude
    }
}
