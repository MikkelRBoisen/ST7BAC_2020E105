package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.st7bac_2020e105.Model.VehicleLocation;
import com.example.st7bac_2020e105.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class EmergencyVehicleLocationActivity extends AppCompatActivity implements View.OnClickListener {

    //Inspired by:
    // https://www.youtube.com/watch?v=Nn4-Vn7qk9k&t=6s&ab_channel=CodinginFlow
    // https://stackoverflow.com/questions/34259618/android-using-imageview-onclick-to-change-image-back-and-forth
    // https://www.youtube.com/watch?v=hyi4dLyPtpI&t=2443s&ab_channel=ProgrammerWorld

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

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public String username;

    ImageView mapImage;
    Integer[] image = {R.drawable.hiclipart, R.drawable.hiclipart1,R.drawable.hiclipart2, R.drawable.hiclipart3};


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


        infoText = findViewById(R.id.txt_info_emergencyvehiclelocation);
        lati = (TextView) findViewById(R.id.txtLat);
        longi = (TextView) findViewById(R.id.txtLong);

        mapImage = (ImageView)findViewById(R.id.img_staticMaps);
        mapImage.setImageResource(R.drawable.hiclipart);

        Intent data = getIntent();
        vehicleType = getIntent().getStringExtra("Vehicle");
        if (data.hasExtra(MainActivity.EXTRA_USER_LONGITUDE) && data.hasExtra(MainActivity.EXTRA_USER_LATITUDE)) {
            userLatitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LATITUDE, 0);
            userLongitude = data.getDoubleExtra(MainActivity.EXTRA_USER_LONGITUDE, 0);
        }
    }


    // Start and stop an emergency on button click
    @Override
    public void onClick(View v) {
        if (v == startStopLocation) {
            if (startStopLocation != null && greenButtonIsVisible) {
                startStopLocation.setBackgroundResource(R.drawable.roedknap);
                greenButtonIsVisible = false;
                infoText.setText(R.string.txt_info_stop);
                writeToFirebase = true;
                switchPhotos();
            }
            else {
                if (startStopLocation != null) {
                    startStopLocation.setBackgroundResource(R.drawable.groenknap);
                    greenButtonIsVisible = true;
                    infoText.setText(R.string.txt_info_start);
                    writeToFirebase = false;
                    mapImage.setImageResource(R.drawable.hiclipart);
                    stopTimer();
                }
            }
        }
    }


    //Loop the image to represent data upload on WIFI icon:
    private int position = -1;
    Timer timer = new Timer();
    private void switchPhotos() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position++;
                if (position >= image.length)
                    position = 0;
                    mapImage.setImageResource(image[position]);
            }
        },0,800);
    }
    private void stopTimer()
    {
        this.timer.cancel();
    }

    // Write to database using a BroadcastReceiver
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

                    todaysDate = dateFormat.format(new Date());
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

    //creating menu with log out
    //inflating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.emegency_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Menu item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.logOut){
            auth.signOut();
            Toast.makeText(EmergencyVehicleLocationActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}