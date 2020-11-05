package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;
import com.google.type.LatLng;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EmergencyVehicleLocationActivity extends AppCompatActivity implements View.OnClickListener {

    //Button: Inspired by https://www.youtube.com/watch?v=Nn4-Vn7qk9k&t=6s&ab_channel=CodinginFlow
    // and https://stackoverflow.com/questions/34259618/android-using-imageview-onclick-to-change-image-back-and-forth

    //https://www.youtube.com/watch?v=hyi4dLyPtpI&t=2443s&ab_channel=ProgrammerWorld


    private DatabaseReference databaseReference;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    String defualtDate = dateFormat.format(new Date(0));
    String todaysDate = dateFormat.format(new Date());
    VehicleLocation vehicleLocation = new VehicleLocation(0,0,"","", defualtDate);

    Button startStopLocation;
    private boolean greenButtonIsVisible = true;
    private boolean writeToFirebase = false;

    private double userLatitude;
    private double userLongitude;

    TextView longi;
    TextView lati;
    TextView infoText;
    private String vehicleType;
    private FirebaseAuth firebase;

    //TEST
    Button test123123;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    Button logOut;


    ArrayList<VehicleLocation> vehicleLocationArray = new ArrayList<>();
    ArrayList<ArrayList<VehicleLocation>> arrayListArrayList = new ArrayList<>();

    HashMap<String, VehicleLocation> map = new HashMap<String, VehicleLocation>();

    public String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_vehicle_location);
        setTitle("Alarm");

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        startStopLocation = findViewById(R.id.btn_startstoplocations);
        startStopLocation.setOnClickListener(EmergencyVehicleLocationActivity.this);

        //Trim the email to get the corret Child-name in Firebase
        String email = auth.getCurrentUser().getEmail().toString();
        username = email.substring(0, email.lastIndexOf("@"));



        logOut = (Button)findViewById(R.id.btn_logOut);

        //TEST
        test123123 = (Button)findViewById(R.id.btn_test123);
        test123123.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleLocationArray.size();
                test123123.setText(String.valueOf(vehicleLocationArray.size()));
            }
        });
        //END OF TEST

        infoText = findViewById(R.id.txt_info_emergencyvehiclelocation);
        lati = (TextView) findViewById(R.id.txtLat);
        longi = (TextView) findViewById(R.id.txtLong);

        Intent data = getIntent();
        vehicleType = getIntent().getStringExtra("Vehicle");
        if (data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)) {
            userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
            userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
        }

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Toast.makeText(EmergencyVehicleLocationActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                finish();
            }
        });





//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren())
//                {
//                    final String key = ds.getKey();
//
//                    databaseReference.child(key).orderByChild(key).limitToLast(1).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            VehicleLocation vehicleLocationzz = new VehicleLocation();
//                            vehicleLocationzz = snapshot.getChildren().iterator().next().getValue(VehicleLocation.class);
//
//                            //Get the current time of the system
//                            long miliSec = System.currentTimeMillis();
//                            //Insert systemCurrentTime to the date format: yyyy-MM-dd HH:mm:sss
//                            String currentDate = dateFormat.format(miliSec);
//
//                            String databaseTimeSeconds = vehicleLocationzz.timestamp.substring(0,16);
//                            String systemTimeSeconds = currentDate.substring(0,16);
//
//                                //https://stackoverflow.com/questions/23283118/comparing-two-time-in-strings
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                                try {
//                                    Date databaseTimeDate = sdf.parse(databaseTimeSeconds);
//                                    Date systemTimeDate = sdf.parse(systemTimeSeconds);
//
//                                    //Compare time elapsed between the two timestamps
//                                    long elapsed = systemTimeDate.getTime() - databaseTimeDate.getTime();
//                                    //https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
//                                    int convertLongToInt = (int) elapsed;
//                                    //Convert from milliseconds to minutes
//                                    int timeBetweenTimeDates = convertLongToInt/60000;
//
//                                    //if timestamp from database is more than 5 min older, don't add to map:
//                                    if (timeBetweenTimeDates<=5)
//                                    {
//                                        map.put(snapshot.getKey(), vehicleLocationzz);
//                                    }
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                        }
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                        }
//                    });
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
    }


    @Override
    public void onClick(View v) {
        if (v == startStopLocation) {
            if (startStopLocation != null && greenButtonIsVisible) {
                startStopLocation.setBackgroundResource(R.drawable.roedknap);
                greenButtonIsVisible = false;
                infoText.setText(R.string.txt_info_stop);
                writeToFirebase = true;
          //      databaseReference.child(username).push().setValue(true);
            }
            else {
                if (startStopLocation != null) {
                    startStopLocation.setBackgroundResource(R.drawable.groenknap);
                    greenButtonIsVisible = true;
                    infoText.setText(R.string.txt_info_start);
                    writeToFirebase = false;

//                    databaseReference.child(username).push().setValue(false);
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
                    vehicleLocation.setVehicleType(vehicleType);

                    userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
                    longi.setText(Double.toString(userLongitude));
                    vehicleLocation.setLongitude(userLongitude);

                    vehicleLocation.setUserId(username);

                    vehicleLocation.setTimestamp(todaysDate);

                    databaseReference.child(username).push().setValue(vehicleLocation);
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