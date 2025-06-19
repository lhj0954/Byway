package com.example.byway;

import java.io.Serializable;

import java.util.Date;

public class SpotData implements Serializable {
    public String id;
    public double latitude;
    public double longitude;
    public String address;
    public String keyword;
    public String description;
    public String imageUrl;
    public String locationText;
    public String createdBy;
    public Date createdAt;
    public SpotData() {}

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getId() { return id; }

    public SpotData( double latitude, double longitude, String address, String keyword, String description, String imageUrl, String locationText, String createdBy, Date createdAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.keyword = keyword;
        this.description = description;
        this.imageUrl = imageUrl;
        this.locationText = locationText;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}
