package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;

import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.st7bac_2020e105.Model.VehicleLocation;
import com.example.st7bac_2020e105.R;
import com.example.st7bac_2020e105.Service;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.LatLng;

import java.lang.reflect.Array;
import java.util.Arrays;

public class EmergencyVehicleLocationActivity extends AppCompatActivity implements View.OnClickListener {

    //Button: Inspired by https://www.youtube.com/watch?v=Nn4-Vn7qk9k&t=6s&ab_channel=CodinginFlow
    // and https://stackoverflow.com/questions/34259618/android-using-imageview-onclick-to-change-image-back-and-forth

    //https://www.youtube.com/watch?v=hyi4dLyPtpI&t=2443s&ab_channel=ProgrammerWorld


    private DatabaseReference databaseReference;
    VehicleLocation vehicleLocation = new VehicleLocation();

    Button startStopLocation;
    private boolean greenButtonIsVisible = true;
    private boolean writeToFirebase = false;

    private double userLatitude;
    private double userLongitude;

    TextView longi;
    TextView lati;
    TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_vehicle_location);

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        startStopLocation = findViewById(R.id.btn_startstoplocations);
        startStopLocation.setOnClickListener(EmergencyVehicleLocationActivity.this);


        infoText = findViewById(R.id.txt_info_emergencyvehiclelocation);
        lati = (TextView) findViewById(R.id.txtLat);
        longi = (TextView) findViewById(R.id.txtLong);

        Intent data = getIntent();
        if (data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)) {
            userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
            userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
        }
    }


    @Override
    public void onClick(View v) {
        if (v == startStopLocation) {
            if (startStopLocation != null && greenButtonIsVisible) {
                startStopLocation.setBackgroundResource(R.drawable.roedknap);
                greenButtonIsVisible = false;
                infoText.setText(R.string.txt_info_stop);
                writeToFirebase = true;
            }
            else {
                if (startStopLocation != null) {
                    startStopLocation.setBackgroundResource(R.drawable.groenknap);
                    greenButtonIsVisible = true;
                    infoText.setText(R.string.txt_info_start);
                    writeToFirebase = false;
                }
            }
        }
    }


    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            if(data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)){
                if (writeToFirebase == true)
                {
                    userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
                    lati.setText(Double.toString(userLatitude));
                    vehicleLocation.setLatitude(userLatitude);

                    userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
                    longi.setText(Double.toString(userLongitude));
                    vehicleLocation.setLongitude(userLongitude);

                    databaseReference.child("latitude").push().setValue(vehicleLocation.getLatitude());
                    databaseReference.child("longitude").push().setValue(vehicleLocation.getLongitude());
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter("LOCATION_UPDATE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
    }
}