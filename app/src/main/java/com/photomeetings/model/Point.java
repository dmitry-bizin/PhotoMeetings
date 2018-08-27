package com.photomeetings.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class Point implements Serializable {

    private float lat;
    private float lng;
    private String address;

    public Point(float lat, float lng, @Nullable String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    public Point() {
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
