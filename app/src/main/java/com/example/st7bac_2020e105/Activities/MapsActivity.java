package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.Toast;

import com.example.st7bac_2020e105.Alarm;
import com.example.st7bac_2020e105.DistanceCalculatorAlgorithm;
import com.example.st7bac_2020e105.Model.VehicleItem;
import com.example.st7bac_2020e105.Model.VehicleLocation;
import com.example.st7bac_2020e105.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private double userLatitude;
    private double userLongitude;
    private boolean userLocationKnown = false;

    private double databaselatitude;
    private double databaselongtitude;

    private int radiusSettings;
    private int radius = 500;
    HashMap<String, VehicleLocation> map = new HashMap<String, VehicleLocation>();


    private DatabaseReference databaseReference;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    String defualtDate = dateFormat.format(new Date(0));
    String todaysDate = dateFormat.format(new Date());
    VehicleLocation vehicleLocation = new VehicleLocation(0,0,"","", defualtDate);

    DistanceCalculatorAlgorithm distanceCalculatorAlgorithm = new DistanceCalculatorAlgorithm();
    private double distanceBetweenCoordinates = 0;
    Alarm alarm = new Alarm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //setting title for MapsActivity
        //https://stackoverflow.com/questions/3975550/android-how-to-change-the-application-title
        setTitle("Maps");

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        ArrayList<VehicleLocation> vehicleLocationArray = new ArrayList<>();

        //Alarm broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Alarm"));

        //Radius broadcast receiver from SettingsActivity
        LocalBroadcastManager.getInstance(this).registerReceiver(safeReceiver, new IntentFilter("SafeIntent"));

        Intent data = getIntent();
        if(data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)){
            userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
            userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
            if(userLatitude!=0 && userLongitude!=0) {
                userLocationKnown = true;
            }
        }

//        radiusSettings = data.getIntExtra("radius",500);
//        if (radiusSettings != radius){
//            radius = radiusSettings;
//        }
        setUpMapIfNeeded();


        databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren())
            {
                final String key = ds.getKey();

                databaseReference.child(key).orderByChild(key).limitToLast(1).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        VehicleLocation vehicleLocationzz = new VehicleLocation();
                        vehicleLocationzz = snapshot.getChildren().iterator().next().getValue(VehicleLocation.class);

                        //Get the current time of the system
                        long miliSec = System.currentTimeMillis();
                        //Insert systemCurrentTime to the date format: yyyy-MM-dd HH:mm:sss
                        String currentDate = dateFormat.format(miliSec);

                        String databaseTimeSeconds = vehicleLocationzz.timestamp.substring(0,16);
                        String systemTimeSeconds = currentDate.substring(0,16);

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
                            int timeBetweenTimeDates = convertLongToInt/60000;

                            //if timestamp from database is more than 5 min older, don't add to map:
                            if (timeBetweenTimeDates<=5)
                            {
                                map.put(snapshot.getKey(), vehicleLocationzz);
                                setUpMap();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    });
}

    //Broadcast receiver for Radius from SettingsActivity:
    private BroadcastReceiver safeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            radiusSettings = intent.getIntExtra("radius",500);
            if (radiusSettings != radius){
                radius = radiusSettings;
                setUpMap();
            }
        }
    };

    //Broadcast receiver for alarm:
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (distanceBetweenCoordinates<=radius)
            {
                alarm.playAlarm(MapsActivity.this);
            }
            if (distanceBetweenCoordinates>=radius)
            {
                alarm.stopAlarm(MapsActivity.this);
            }
        }
    };

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);  //this is the new way
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
            zoomToUser();
        }
    }

    private void setUpMap() {
        if(userLocationKnown) {
            //Clear map from old markers
            mMap.clear();

            for (Map.Entry<String, VehicleLocation> item : map.entrySet()) {
                VehicleLocation value = item.getValue();
                value.getVehicleType();
                if(value.vehicleType.equals("Ambulance")) {
                    MarkerOptions AmbulanceVehicle = new MarkerOptions().position(new LatLng(value.latitude, value.longitude));
                    AmbulanceVehicle.icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulance));
                    mMap.addMarker(AmbulanceVehicle);
                }
                if(value.vehicleType.equals("Firetruck")) {
                    MarkerOptions FireTruck = new MarkerOptions().position(new LatLng(value.latitude, value.longitude));
                    FireTruck.icon(BitmapDescriptorFactory.fromResource(R.drawable.firetruck));
                    mMap.addMarker(FireTruck);
                }
                //Play alarm through broadcast intent if distance between coordinates is bigger than the radius
                distanceBetweenCoordinates = distanceCalculatorAlgorithm.DistanceCalculatorAlgorithm(userLatitude,userLongitude,value.latitude,value.longitude);
                //if (distanceBetweenCoordinates<=radius)
                //{
                    Intent alarmIntent = new Intent("Alarm");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(alarmIntent);
               // }
            }

//            {
//                databaselatitude =  map.values().iterator().next().latitude;
//                databaselongtitude = map.values().iterator().next().longitude;
//
//                if(map.values().iterator().next().vehicleType.equals("Ambulance")) {
//                    MarkerOptions AmbulanceVehicle = new MarkerOptions().position(new LatLng(databaselatitude, databaselongtitude));
//                    AmbulanceVehicle.icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulance));
//                    mMap.addMarker(AmbulanceVehicle);
//                }
//                if(map.values().iterator().next().vehicleType.equals("Brandbil")) {
//                    MarkerOptions FireTruck = new MarkerOptions().position(new LatLng(databaselatitude, databaselongtitude));
//                    FireTruck.icon(BitmapDescriptorFactory.fromResource(R.drawable.brandbil));
//                    mMap.addMarker(FireTruck);
//                }
//            }
            //Create marker
            MarkerOptions Usermarker = new MarkerOptions().position(new LatLng(userLatitude, userLongitude)).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_car));
            mMap.addMarker(Usermarker);
            LatLng latlng = new LatLng(userLatitude,userLongitude);

            //Add circle setup 500m
            CircleOptions myCircle = new CircleOptions()
                    .center(latlng)
                    .radius(radius);
            //plot in google maps - https://developers.google.com/android/reference/com/google/android/gms/maps/model/Circle
            Circle circle = mMap.addCircle(myCircle);
            circle.setStrokeColor(Color.RED);
            circle.setFillColor(0x220000FF);
            zoomToUser();
        }
    }




    private void zoomToUser(){
        if(userLocationKnown) {
            if(radius == 500){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLatitude, userLongitude), 15));
            }
            if(radius <= 499){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLatitude, userLongitude), 17));
            }
            if(radius >= 501 && radius <= 750){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLatitude, userLongitude), 14));
            }
            if(radius >= 751){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLatitude, userLongitude), 13));
            }

        } else {
            Toast.makeText(getApplicationContext(), "User location unknown", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter("LOCATION_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Alarm"));
        LocalBroadcastManager.getInstance(this).registerReceiver(safeReceiver, new IntentFilter("SafeIntent"));
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
    }


    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            //Toast.makeText(getApplicationContext(), "Got location update", Toast.LENGTH_SHORT).show();
            if(data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)){
                userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
                userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
                if(userLatitude!=0 && userLongitude!=0) {
                    userLocationKnown = true;
                }
                
            }
            setUpMap();
        }
    };


    //creating menu with items; Beredskabslogin & settings
    //inflating options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Menu items selected


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.item_Beredskabslogin){
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        }
        if(id == R.id.item_Settings){
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}