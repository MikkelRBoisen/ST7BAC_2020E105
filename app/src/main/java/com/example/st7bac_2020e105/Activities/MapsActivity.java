package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.st7bac_2020e105.DistanceCalculatorAlgorithm;
import com.example.st7bac_2020e105.Model.TimeCalculator;
import com.example.st7bac_2020e105.Model.VehicleLocation;
import com.example.st7bac_2020e105.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private double userLatitude;
    private double userLongitude;
    private boolean userLocationKnown = false;
    Button navigation_follow_user;
    private int radiusSettings;
    private int radius = 500;
    HashMap<String, VehicleLocation> map = new HashMap<String, VehicleLocation>();
    HashMap<String, VehicleLocation> alarmingmap = new HashMap<String, VehicleLocation>();

    private DatabaseReference databaseReference;
    boolean cameraSet = false;

    private TextToSpeech textToSpeech;
    String address;
    String addressCorrect;
    String finalAddress;
    private int count = 0;

    DistanceCalculatorAlgorithm distanceCalculatorAlgorithm = new DistanceCalculatorAlgorithm();
    private double distanceBetweenCoordinates = 0;

    TimeCalculator timeCalculator = new TimeCalculator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        //setting title for MapsActivity
        //https://stackoverflow.com/questions/3975550/android-how-to-change-the-application-title
        setTitle(getString(R.string.maps));

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");

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

        navigation_follow_user = (Button)findViewById(R.id.navigation_button);
        navigation_follow_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToUser();
            }
        });

        setUpMapIfNeeded();

        // Read data from Firebase onDataChange
        databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren())
            {
                // Retrieve all keys in database:
                final String key = ds.getKey();
                // Run trough the last child in each key:
                databaseReference.child(key).orderByChild(key).limitToLast(1).addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        VehicleLocation vehicleLocations;
                        vehicleLocations = snapshot.getChildren().iterator().next().getValue(VehicleLocation.class);
                        try {
                            int timeBetweenDates = timeCalculator.CheckTime(vehicleLocations);
                            //if timestamp from database is more than 5* min older, don't add to map:
                            if (timeBetweenDates<=5)
                            {
                                map.put(snapshot.getKey(), vehicleLocations);
                                setUpMap();
                            }
                            if (timeBetweenDates > 5)
                            {
                                map.remove(snapshot.getKey());
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
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            radiusSettings = intent.getIntExtra("radius",500);
            if (radiusSettings != radius){
                radius = radiusSettings;
                setUpMap();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
            zoomToUser();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpMap() {
        if(userLocationKnown) {
            //Clear map from old markers
            mMap.clear();

            for (Map.Entry<String, VehicleLocation> item : map.entrySet()) {
                final VehicleLocation value = item.getValue();
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
                if(value.vehicleType.equals("Medical car")) {
                    MarkerOptions FireTruck = new MarkerOptions().position(new LatLng(value.latitude, value.longitude));
                    FireTruck.icon(BitmapDescriptorFactory.fromResource(R.drawable.laegebil));
                    mMap.addMarker(FireTruck);
                }

                //Play alarm through broadcast intent if distance between coordinates is bigger than the radius
                distanceBetweenCoordinates = distanceCalculatorAlgorithm.DistanceCalculatorAlgorithm(userLatitude,userLongitude,value.latitude,value.longitude);

                //second hashmap containing only vehicles within raidus distance for controlling "circle-alarm"-plot
                if(distanceBetweenCoordinates <= radius){
                    alarmingmap.put(value.userId,value);
                }
                else{
                    alarmingmap.remove(value.userId,value);
                }

                //Get the address from coordinates:
                Geocoder geocoder;
                final List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(value.latitude, value.longitude, 1);
                    //Preventing program crash for unknown addresses
                    if (!addresses.isEmpty())
                    {
                        address = addresses.get(0).getAddressLine(0);
                        //Removing street number, postal code, city name and country:
                        addressCorrect = address.split(",")[0];
                        finalAddress = addressCorrect.replaceAll("[^A-Åa-å + //]", "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Calculating distance between users location and emergency vehicles - the implemented method in android.
//                float[] results = new float[1];
//                Location.distanceBetween(userLatitude,userLongitude,value.latitude,value.longitude,results);
//                float distance = results[0];
//                if(distance <=radius){
//                    startalarming = 1;
//                }
//                else{
//                    startalarming=0;
//                }
            }

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
            zoomToUser();
            //setting up notification

            if(!alarmingmap.isEmpty()){
                circle.setFillColor(0x220000FF);
                circle.setStrokeColor(Color.RED);

                count++;
                readEmegencyLocationALoud();

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel channel = new NotificationChannel("My Noticiation","my notification",NotificationManager.IMPORTANCE_LOW);
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(channel);
                }

                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                String CHANNEL_ID="Alarm";
                NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"Udrykning",NotificationManager.IMPORTANCE_LOW);
                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
                Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                        .setContentText("Der er en udrykning i nærheden af dig. Se hvor...")
                        .setContentTitle("Udrykning indenfor " +radius+" meter")
                        .setContentIntent(pendingIntent)
                        .addAction(android.R.drawable.sym_action_chat,"Beware",pendingIntent)
                        .setChannelId(CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .build();

                NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
                notificationManager.notify(96,notification);

            }
            else{
                int strokecolor = Color.parseColor("#07675E");
                circle.setStrokeColor(strokecolor);
                count = 0;
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(96);
            }
        }
    }

    private void readEmegencyLocationALoud() {
        //text to speech the address and vehicle type
        if (count ==1)
        {
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS)
                    {
                        //På dansk:
                        Locale loc = new Locale("da","DK");
                        textToSpeech.setLanguage(loc);

                        /** På engelsk:
                        //textToSpeech.setLanguage(Locale.ENGLISH); */

                        double speachrate = 0.85;
                        float speachrateFloat = (float)speachrate;
                        textToSpeech.setSpeechRate(speachrateFloat);

                        //Calculate distance from alarmingmap. Convert from double to integer, to be read out loud:
                        double distanceEmegencyCar = distanceCalculatorAlgorithm.DistanceCalculatorAlgorithm(userLatitude, userLongitude, alarmingmap.values().iterator().next().latitude, alarmingmap.values().iterator().next().longitude);
                        int readDistance = (int)Math.round(distanceEmegencyCar);

                        String speakAloud;
                        if (finalAddress == null)
                        {
                            //Ved en ukendt adresse
                            String saySomething = "Vær opmærksom på "+ alarmingmap.values().iterator().next().vehicleType + " om " + readDistance + " meter";
                            speakAloud = saySomething;
                        }
                        else{
                            //Ved en kendt adresse
                            String saySomething = "Vær opmærksom på "+ alarmingmap.values().iterator().next().vehicleType + " ved " + finalAddress + " om " + readDistance + " meter";
                            speakAloud = saySomething;
                        }
                        textToSpeech.speak(speakAloud, TextToSpeech.QUEUE_ADD, null);
                        count++;
                    }
                }
            });
        }
    }

    private void zoomToUser( ){
        if(userLocationKnown) {
                if(radius == 500){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userLatitude, userLongitude), 15));
                }
                if(radius <= 499){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userLatitude, userLongitude), 17));
                    cameraSet =true;
                }
                if(radius >= 501 && radius <= 750){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userLatitude, userLongitude), 15));
                    cameraSet =true;
                }
                if(radius >= 751){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userLatitude, userLongitude), 14));
                    cameraSet =true;
                }
        } else {
            Toast.makeText(getApplicationContext(), "User location unknown", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter("LOCATION_UPDATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(safeReceiver, new IntentFilter("SafeIntent"));
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
    }

    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
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
            finish();
        }
        if(id == R.id.item_Settings){
            Intent radiusIntent = new Intent(MapsActivity.this, SettingsActivity.class);
            radiusIntent.putExtra("RadiusIntent", radius);
            startActivity(radiusIntent);
            //startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}