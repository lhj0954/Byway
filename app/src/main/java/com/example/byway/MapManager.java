package com.example.byway;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.PolylineOverlay;

import java.util.List;

public class MapManager {
    private final PolylineOverlay polylineOverlay = new PolylineOverlay();
    private final NaverMap naverMap;

    public MapManager(NaverMap naverMap) {
        this.naverMap = naverMap;
    }

    public void updatePath(List<LatLng> path) {
        if (path.size() >= 2) {
            polylineOverlay.setCoords(path);
            polylineOverlay.setWidth(10); //선 두께
            polylineOverlay.setColor(0xFFa47864); //빨간색 0xAAFF0000
            polylineOverlay.setMap(naverMap);
        }
    }

    public void clearPath() {
        polylineOverlay.setMap(null);
    }
}
