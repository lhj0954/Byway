package com.example.byway;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;

public class SpotManager {
    private Marker spotMarker;
    private LatLng selectedSpot;

    public void addSpotMarker(NaverMap naverMap, LatLng spot) {
        selectedSpot = spot;
        //기존에 마커있으면 제거
        if (spotMarker != null) {
            spotMarker.setMap(null);
        }
        spotMarker = new Marker();
        spotMarker.setPosition(selectedSpot);
        spotMarker.setMap(naverMap);
    }

    public void clearSpotMarker() {
        if (spotMarker != null) {
            spotMarker.setMap(null);
            spotMarker = null;
        }
        selectedSpot = null;
    }

    public LatLng getSelectedSpot() {
        return selectedSpot;
    }
}

