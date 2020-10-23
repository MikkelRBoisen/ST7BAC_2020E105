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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private double userLatitude;
    private double userLongitude;
    private boolean userLocationKnown = false;
    private int radiusSettings;
    private int radius = 500;

    private DatabaseReference databaseReference;
    // ArrayList<VehicleLocation> vehicleLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //setting title for MapsActivity
        //https://stackoverflow.com/questions/3975550/android-how-to-change-the-application-title
        setTitle("Maps");


        Intent data = getIntent();
        if(data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)){
            userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
            userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
            if(userLatitude!=0 && userLongitude!=0) {
                userLocationKnown = true;
            }
        }
        radiusSettings = data.getIntExtra("radius",500);
        if (radiusSettings != radius){
            radius = radiusSettings;
        }
        setUpMapIfNeeded();
    }

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

            //Create marker
            MarkerOptions marker = new MarkerOptions().position(new LatLng(userLatitude, userLongitude)).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_car));
            mMap.addMarker(marker);
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
            startActivity(new Intent(MapsActivity.this, EmergencyVehicleLocationActivity.class));
        }
        if(id == R.id.item_Settings){
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}