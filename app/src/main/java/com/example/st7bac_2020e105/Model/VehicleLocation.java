package com.example.st7bac_2020e105.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by "TheArnieExerciseFinder" model class
 */

public class VehicleLocation {

    private double latitude;
    private double longitude;
    private String vehicleType;


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

    public double getLongitude() {
        return longitude;
    }

    public String getVehicleType() {
        return vehicleType;
    }


}
