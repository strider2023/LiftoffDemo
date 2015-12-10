package com.liftoff.demo.dao;

/**
 * Created by arindamnath on 09/12/15.
 */
public class Location {

    private double latitude,longitude;
    private String address;

    public Location(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
