package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.st7bac_2020e105.Model.VehicleLocation;
import com.example.st7bac_2020e105.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final long MIN_TIME_BETWEEN_LOCATION_UPDATES = 2000;    // milisecs
    private static final float MIN_DISTANCE_MOVED_BETWEEN_LOCATION_UPDATES = 50;  // meters
    public static final String EXTRA_USER_LATITUDE = "location_latitude";
    public static final String EXTRA_USER_LONGITUDE = "location_longitude";

    public static final int PERMISSIONS_REQUEST_LOCATION = 189;

    private boolean isTracking = false;
    private LocationManager locationManager;
    private Location userLocation;
    private double latitude, longitude;

    Button test1;
    Button test2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test1 = (Button)findViewById(R.id.btnTest1);
        test2 = (Button)findViewById(R.id.btnTest2);

        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                if (userLocation != null) {
                    //if location known, send it to the map activity
                    mapIntent.putExtra(EXTRA_USER_LATITUDE, userLocation.getLatitude());
                    mapIntent.putExtra(EXTRA_USER_LONGITUDE, userLocation.getLongitude());
                }
                startActivity(mapIntent);
            }
        });
        checkPermissions();
        toogleTracking();
        updateStatus();
    }

    private void toogleTracking() {

        if (isTracking) {
            stopTracking();
        } else {
            startTracking();
        }
        updateStatus();
    }

    private boolean startTracking() {
        try {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }

            long minTime = MIN_TIME_BETWEEN_LOCATION_UPDATES;
            float minDistance = MIN_DISTANCE_MOVED_BETWEEN_LOCATION_UPDATES;
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

            if (locationManager != null) {
                try {
                    locationManager.requestLocationUpdates(minTime, minDistance, criteria, locationListener, null);
                    //Use criteria to chose best provider
                } catch (SecurityException ex) {
                    // user have disabled location permission - need to validate this permission for newer versions
                }
            } else {
                return false;
            }
            isTracking = true;
            return true;
        } catch (Exception ex) {
            //things can go wrong
            Log.e("TRACKER", "Error during start", ex);
            return false;
        }
    }

    private boolean stopTracking() {
        try {
            try {
                locationManager.removeUpdates(locationListener);
                isTracking = false;
            } catch (SecurityException ex) {
                //user have disabled location permission - need to validate this permission for newer versions
            }
            return true;
        } catch (Exception ex) {
            //things can go wrong here as well (listener is null)
            Log.e("TRACKER", "Error during stop", ex);
            return false;
        }
    }

    private void updateStatus() {
        if (userLocation != null) {
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
        }

        if (userLocation == null && locationManager != null) {
            //permission check - even though we do get this permission when the app starts up
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //do nothing
                Toast.makeText(this, "Need permission for location", Toast.LENGTH_SHORT).show();
            } else {
                Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
    }

    private void broadcastLocationUpdate(Location location){
        Intent update = new Intent("LOCATION_UPDATE");
        update.putExtra(EXTRA_USER_LATITUDE, location.getLatitude());
        update.putExtra(EXTRA_USER_LONGITUDE, location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(update);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            userLocation = location;
            updateStatus();
            broadcastLocationUpdate(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // got permission
                } else {
                    // permission denied
                    //in this case we just close the app
                    Toast.makeText(this, "You need to enable permission for Location to use the app", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }









}