package com.example.st7bac_2020e105.Model;

import com.google.type.DateTime;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by "TheArnieExerciseFinder" model class
 */

public class VehicleLocation {

    public double latitude;
    public double longitude;
    public String vehicleType;
    public String userId;
    public String timestamp;

    public VehicleLocation(){    }

    public VehicleLocation(double lati, double longi, String type, String userId, String timestamp)
    {
        this.latitude = lati;
        this.longitude = longi;
        this.vehicleType = type;
        this.userId = userId;
        this.timestamp = timestamp;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
