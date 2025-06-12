package com.example.byway;

import com.naver.maps.geometry.LatLng;
import java.util.List;
import java.util.Date;


public class PathData {
    private String createdBy;
    private Date createdAt; // UNIX timestamp (millis)
    private String keyword;
    private List<LatLng> path;

    public PathData() { }

    public PathData(String createdBy, Date createdAt, String keyword, List<LatLng> path) {
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.keyword = keyword;
        this.path = path;
    }

    // getter + setter 쌍 모두 추가
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<LatLng> getPath() {
        return path;
    }
    public void setPath(List<LatLng> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "PathData{" +
                "createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", keyword='" + keyword + '\'' +
                ", pathSize=" + (path != null ? path.size() : 0) +
                '}';
    }
}
