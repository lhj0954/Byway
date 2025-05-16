package com.example.byway;

import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PathRecorder {
    private final List<LatLng> path = new ArrayList<>();

    public void addPoint(LatLng point) {
        path.add(point);
    }

    public List<LatLng> getPath() {
        return path;
    }

    public void clear() {
        path.clear();
    }

    public boolean hasEnoughPoints() {
        return path.size() >= 2;
    }
}
