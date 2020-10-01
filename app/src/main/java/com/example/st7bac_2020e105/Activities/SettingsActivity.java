package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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



    //Adjusting volume
    SeekBar volume;
    AudioManager audioManager;

    SeekBar radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // TEST LYD MED KNAP
        testLydKnap = (Button)findViewById(R.id.btnTestSound);
        testLydKnap.setOnClickListener(this);
        //



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

    }

    //TEST LYD MED KNAP OG SERVICE
    @Override
    public void onClick(View v) {
        if (v == testLydKnap)
        startService(new Intent(this, Service.class));
        //PlaySound();
    }
}