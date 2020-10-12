package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.st7bac_2020e105.R;
import com.example.st7bac_2020e105.Service;

public class EmergencyVehicleLocationActivity extends AppCompatActivity {

    //Button: Inspired by https://www.youtube.com/watch?v=Nn4-Vn7qk9k&t=6s&ab_channel=CodinginFlow

    //https://codelabs.developers.google.com/codelabs/realtime-asset-tracking/index.html?index=..%2F..index#2

    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_vehicle_location);

        Button startStopLocation = findViewById(R.id.btn_startstoplocations);
        TextView infoText = findViewById(R.id.txt_info_emergencyvehiclelocation);

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }


        startStopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Start
                //Stop
                //Ændrer tekst og knap-billede
            }
        });


    }
    private void startService()
    {
        startService(new Intent(this, Service.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startService();
        } else {
            finish();
        }
    }
}