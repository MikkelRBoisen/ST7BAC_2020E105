package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.st7bac_2020e105.Alarm;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private double userLatitude;
    private double userLongitude;
    private boolean userLocationKnown = false;
    Button navigation_follow_user;
    private double databaselatitude;
    private double databaselongtitude;
    private int startalarming = 0;
    private int radiusSettings;
    private int radius = 500;
    HashMap<String, VehicleLocation> map = new HashMap<String, VehicleLocation>();
    HashMap<String, VehicleLocation> alarmingmap = new HashMap<String, VehicleLocation>();


    private DatabaseReference databaseReference;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    String defualtDate = dateFormat.format(new Date(0));
    String todaysDate = dateFormat.format(new Date());
    VehicleLocation vehicleLocation = new VehicleLocation(0,0,"","", defualtDate);
    boolean cameraSet = false;

    private TextToSpeech textToSpeech;
    String address;

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

        navigation_follow_user = (Button)findViewById(R.id.navigation_button);
        navigation_follow_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToUser();
            }
        });

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
                    @RequiresApi(api = Build.VERSION_CODES.O)
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

    //Broadcast receiver for alarm:
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (distanceBetweenCoordinates<=radius)
            {
                
                //alarm.playAlarm(MapsActivity.this);
                //alarm.playAlarm(MapsActivity.this);
            }
            if (distanceBetweenCoordinates>=radius)
            {
                //alarm.stopAlarm(MapsActivity.this);
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

//                //Get the current time of the system
//                long miliSec = System.currentTimeMillis();
//                //Insert systemCurrentTime to the date format: yyyy-MM-dd HH:mm:sss
//                String currentDate = dateFormat.format(miliSec);
//
//                String databaseTimeSeconds = value.timestamp.substring(0,16);
//                String systemTimeSeconds = currentDate.substring(0,16);
//
//                //https://stackoverflow.com/questions/23283118/comparing-two-time-in-strings
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                try {
//                    Date databaseTimeDate = sdf.parse(databaseTimeSeconds);
//                    Date systemTimeDate = sdf.parse(systemTimeSeconds);
//
//                    //Compare time elapsed between the two timestamps
//                    long elapsed = systemTimeDate.getTime() - databaseTimeDate.getTime();
//                    //https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
//                    int convertLongToInt = (int) elapsed;
//                    //Convert from milliseconds to minutes
//                    int timeBetweenTimeDates = convertLongToInt/60000;
//
//                    //if timestamp from database is more than 5 min older, don't add to map:
//                    if (timeBetweenTimeDates>5){
//                        map.remove();
//                    }
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
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
                    Intent alarmIntent = new Intent("Alarm");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(alarmIntent);

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
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //text to speech the address and vehicle type
                textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS)
                        {
                            textToSpeech.setLanguage(Locale.US);
                            String saySomething = "Beware of the "+ value.vehicleType + " on address "+address;
                            textToSpeech.speak(saySomething, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                });




                //Calculating distance between users location and emergency vehicles
                float[] results = new float[1];
                Location.distanceBetween(userLatitude,userLongitude,value.latitude,value.longitude,results);
                float distance = results[0];
                if(distance <=radius){
                    startalarming = 1;
                }
                else{
                    startalarming=0;
                }
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
            zoomToUser();
            //setting up notification

            if(!alarmingmap.isEmpty()){
                circle.setFillColor(0x220000FF);
                circle.setStrokeColor(Color.RED);

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
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(96);
            }
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
        }
        if(id == R.id.item_Settings){
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}