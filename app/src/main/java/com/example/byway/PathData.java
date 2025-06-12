package com.example.byway;

import com.naver.maps.geometry.LatLng;
import java.util.List;
import java.util.Date;


public class PathData {
    private String createdBy;
    private Date createdAt; // UNIX timestamp (millis)
    private String keyword;
    private List<LatLng> path;

    public PathData(String createdBy, Date createdAt, String keyword, List<LatLng> path) {
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.keyword = keyword;
        this.keyword = keyword;
        this.path = path;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getKeyword() {
        return keyword;
    }

    public List<LatLng> getPath() {
        return path;
    }
}
