package com.example.st7bac_2020e105.Model;

import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCalculator {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    int timeBetweenTimeDates_return;


    public int TimeCalculator(DataSnapshot snapshot) {

        VehicleLocation vehicleLocationzz = new VehicleLocation();
        vehicleLocationzz = snapshot.getChildren().iterator().next().getValue(VehicleLocation.class);

        //Get the current time of the system
        long miliSec = System.currentTimeMillis();
        //Insert systemCurrentTime to the date format: yyyy-MM-dd HH:mm:sss
        String currentDate = dateFormat.format(miliSec);

        String databaseTimeSeconds = vehicleLocationzz.timestamp.substring(0, 16);
        String systemTimeSeconds = currentDate.substring(0, 16);

        //https://stackoverflow.com/questions/23283118/comparing-two-time-in-strings
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date databaseTimeDate = sdf.parse(databaseTimeSeconds);
            Date systemTimeDate = sdf.parse(systemTimeSeconds);

            //Compare time elapsed between the two timestamps
            long elapsed = systemTimeDate.getTime() - databaseTimeDate.getTime();
            //https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
            int convertLongToInt = (int) elapsed;
            //Convert from milliseconds to minutes
            int timeBetweenTimeDates = convertLongToInt / 60000;
             timeBetweenTimeDates_return = timeBetweenTimeDates;
            //if timestamp from database is more than 5 min older, don't add to map:

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeBetweenTimeDates_return;
    }
}
