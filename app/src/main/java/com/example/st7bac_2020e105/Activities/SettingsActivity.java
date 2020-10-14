package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.st7bac_2020e105.Alarm;
import com.example.st7bac_2020e105.R;
import com.example.st7bac_2020e105.Service;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    //Inspiration fra:
    //https://www.youtube.com/watch?v=KAx5OAld8Hg&t=301s&ab_channel=WithSam


    //Test knap:
    //Tilføjet "Implements View.OnClickListenter" - kan fjernes når knappen fjernes..
    Button testLydKnap;
    Button testActivity;
    Button savechangesbtn;


    //Service
    private Service myService;
    private ServiceConnection myConnection;
    boolean bound = false;
    private int newradius;

    //Adjusting volume
    SeekBar volume;
    AudioManager audioManager;

    private int radiuschanged;

    SeekBar radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // TEST LYD MED KNAP
        testLydKnap = (Button)findViewById(R.id.btnTestSound);
        testLydKnap.setOnClickListener(this);
        //

        // TEST MED NEW ACTIVITY
        testActivity = (Button)findViewById(R.id.btnTestNewActivity);
        testActivity.setOnClickListener(this);
        //

        //save changes settings
        savechangesbtn = (Button)findViewById(R.id.btn_safe_settingsactivity);
        savechangesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent radiusIntent =new Intent(SettingsActivity.this, MapsActivity.class);
                radiusIntent.putExtra("radius",radiuschanged);
                startActivity(radiusIntent);
            }
        });


        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        int MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        volume = (SeekBar)findViewById(R.id.seekBar_volume_settingsactivity);
        volume.setMax(MAX_VOLUME);
        volume.setProgress(currentVolume);

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {            }
        });


        radius = (SeekBar)findViewById(R.id.seekBar_radius_settingsactivity);
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Intent radiusIntent =new Intent(SettingsActivity.this, Service.class);
           // radiusIntent.putExtra("radius",progress);
            //bindService(radiusIntent, myConnection, BIND_AUTO_CREATE);
            radiuschanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {            }
        });
    }

//    private void setupServiceConnection(){
//        myConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                myService = ((Service.LocalBinder) service).getService();
//                bound = true;
//            }
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                myService = null;
//            }
//        };
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Bind to Service
//        setupServiceConnection();
//        bindService(new Intent(this, Service.class), myConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Unbind from the service
//        unbindService(myConnection);
//        bound = false;
//    }

    //TEST LYD MED KNAP OG SERVICE
    @Override
    public void onClick(View v) {
        if (v == testLydKnap)
        {
            startService(new Intent(this, Service.class));
            //PlaySound();
        }
        //Test new activity
        if (v == testActivity)
        {
            Intent intentNewActivity = new Intent(v.getContext(), LocationActivity.class);
            v.getContext().startActivity(intentNewActivity);
        }
    }
}