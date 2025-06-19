package com.example.byway;

import com.naver.maps.geometry.LatLng;
import java.util.List;
import java.util.Objects;

public class RouteInfo {
    /** 전체 경로 좌표 리스트 */
    private final List<LatLng> path;
    /** 총 거리 (미터 단위) */
    private final double distance;
    /** 예상 소요 시간 (분 단위) */
    private final int duration;

    /**
     * @param path      병합된 전체 경로 좌표 리스트
     * @param distance  총 거리 (m)
     * @param duration  예상 소요 시간 (분)
     */
    public RouteInfo(List<LatLng> path, double distance, int duration) {
        this.path = path;
        this.distance = distance;
        this.duration = duration;
    }

    /** 경로 좌표 리스트 반환 */
    public List<LatLng> getPath() {
        return path;
    }

    /** 총 거리(m) 반환 */
    public double getDistance() {
        return distance;
    }

    /** 예상 시간(분) 반환 */
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "RouteInfo{" +
                "distance=" + distance + "m, " +
                "duration=" + duration + "min, " +
                "points=" + path.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteInfo)) return false;
        RouteInfo that = (RouteInfo) o;
        return Double.compare(that.distance, distance) == 0 &&
                duration == that.duration &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, distance, duration);
    }
}
