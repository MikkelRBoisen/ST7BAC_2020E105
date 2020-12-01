package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.st7bac_2020e105.R;

public class SettingsActivity extends AppCompatActivity {

    //Inspiration fra:
    //https://www.youtube.com/watch?v=KAx5OAld8Hg&t=301s&ab_channel=WithSam

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

        setTitle("Settings");

        volumeIndicator = (TextView)findViewById(R.id.txt_volumeIndicator);
        radiusIndicator = (TextView)findViewById(R.id.txt_radiusIndicator);


        //Safe changes button
        safeButton = (Button)findViewById(R.id.btn_safe_settingsactivity);
        safeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent safeIntent = new Intent("SafeIntent").putExtra("radius",radiusValue);
                LocalBroadcastManager.getInstance(SettingsActivity.this).sendBroadcast(safeIntent);
                finish();
            }
        });

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        int MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volume = (SeekBar)findViewById(R.id.seekBar_volume_settingsactivity);
        volume.setMax(MAX_VOLUME);
        volume.setProgress(currentVolume);
        volumeIndicator.setText(currentVolume + " %");

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
        Intent getRadiusIntent = getIntent();
        int radiusIntent = getRadiusIntent.getIntExtra("RadiusIntent", 500);
        radius.setMax(0);
        radius.setMax(1000);
        radius.setProgress(radiusIntent);
        radiusIndicator.setText(radiusIntent + " meter");
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