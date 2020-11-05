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
import android.widget.TextView;

import com.example.st7bac_2020e105.Alarm;
import com.example.st7bac_2020e105.R;
import com.example.st7bac_2020e105.Service;

public class SettingsActivity extends AppCompatActivity {

    //Inspiration fra:
    //https://www.youtube.com/watch?v=KAx5OAld8Hg&t=301s&ab_channel=WithSam


    //Test knap:
    //Tilføjet "Implements View.OnClickListenter" - kan fjernes når knappen fjernes..
    Button testLydKnap;
    Button testActivity;


    Button safeButton;

    //Adjusting volume
    SeekBar volume;
    AudioManager audioManager;
    TextView volumeIndicator;

    //Adjusting radius
    SeekBar radius;
    int radiusValue;
    TextView radiusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        volumeIndicator = (TextView)findViewById(R.id.txt_volumeIndicator);
        radiusIndicator = (TextView)findViewById(R.id.txt_radiusIndicator);

        //Safe changes button
        safeButton = (Button)findViewById(R.id.btn_safe_settingsactivity);
        safeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent radiusIntent =new Intent(SettingsActivity.this, MapsActivity.class);
                radiusIntent.putExtra("radius",radiusValue);
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
                volumeIndicator.setText(Integer.toString(progress) + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {            }
        });


        radius = (SeekBar)findViewById(R.id.seekBar_radius_settingsactivity);
        radius.setMax(1000);
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusValue = progress;
                radiusIndicator.setText(Integer.toString(progress) + " meter");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {            }
        });
    }
}