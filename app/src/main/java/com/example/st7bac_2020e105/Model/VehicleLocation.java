package com.example.st7bac_2020e105.Model;

/**
 * Inspired by "TheArnieExerciseFinder" model class
 */

public class VehicleLocation {

    private double latitude;
    private double longitude;
    private String vehicleType;

    public VehicleLocation(double lati, double longi, String type)
    {
        latitude = lati;
        longitude = longi;
        vehicleType = type;
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
