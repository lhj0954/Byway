package com.example.byway;

public class WalkRouteRequest {
    private String start;
    private String goal;
    private String option;

    public WalkRouteRequest(String start, String goal, String option) {
        this.start = start;
        this.goal = goal;
        this.option = option;
    }
}
