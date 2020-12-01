package com.example.st7bac_2020e105;

public class DistanceCalculatorAlgorithm {

    //Inspired by http://www.movable-type.co.uk/scripts/latlong.html?from=49.243824,-121.887340&to=49.235347,-121.92532

    public double DistanceCalculatorAlgorithm(double myLat, double myLong, double emergencyLat, double emergencyLong)
    {
        int earthRadius = 6371;
        //Converting lat and long into radians:
        double myLatInRadians = myLat*Math.PI/180;
        double emergencyLatInRadians = emergencyLat*Math.PI/180;

        //Delta latitude - the distance between the two latitudes (in radians):
        double deltaLat = (emergencyLat - myLat)*Math.PI/180;
        //Delta longitude - the distance between the two longitudes (in radians):
        double deltaLong =(emergencyLong - myLong)*Math.PI/180;

        /** Haversine formula*/
        //a is the square of half the chord length between the points
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(myLatInRadians) * Math.cos(emergencyLatInRadians) *
                   Math.sin(deltaLong/2) * Math.sin(deltaLong/2);
        //c is the angular distance in radians
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        //d is distance in metres:
        double d = (earthRadius*c)*1000;
        return d;
    }
}
