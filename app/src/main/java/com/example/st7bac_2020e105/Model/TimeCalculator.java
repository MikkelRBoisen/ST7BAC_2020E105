package com.example.st7bac_2020e105.Model;

import com.squareup.okhttp.internal.DiskLruCache;
import com.google.firebase.database.DataSnapshot;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCalculator {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    public int CheckTime(VehicleLocation vehicleLocation) throws ParseException {

        //Get the current time of the system
        long miliSec = System.currentTimeMillis();
        //Insert systemCurrentTime to the date format: yyyy-MM-dd HH:mm:sss
        String currentDate = dateFormat.format(miliSec);

        String databaseTimeSeconds = vehicleLocation.timestamp.substring(0,16);
        String systemTimeSeconds = currentDate.substring(0,16);

        //https://stackoverflow.com/questions/23283118/comparing-two-time-in-strings
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date databaseTimeDate = sdf.parse(databaseTimeSeconds);
        Date systemTimeDate = sdf.parse(systemTimeSeconds);

        //Compare time elapsed between the two timestamps
        long elapsed = systemTimeDate.getTime() - databaseTimeDate.getTime();
        //https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
        int convertLongToInt = (int) elapsed;
        //Convert from milliseconds to minutes
        int timeBetweenTimeDates = convertLongToInt/60000;

        return timeBetweenTimeDates;
    }
}
