package com.example.st7bac_2020e105.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by "TheArnieExerciseFinder" model class
 */

public class VehicleLocation {

    public double latitude;
    public double longitude;
    public String vehicleType;


    public VehicleLocation(){    }

    public VehicleLocation(double lati, double longi, String type)
    {
        this.latitude = lati;
        this.longitude = longi;
        this.vehicleType = type;
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

//    public Map<String, Object> toMap()
//    {
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("latitude", latitude);
//        result.put("longitude", longitude);
//        result.put("vehicleType", vehicleType);
//        return result;
//    }
}
