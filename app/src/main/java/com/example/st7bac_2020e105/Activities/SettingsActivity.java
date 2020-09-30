package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;

import com.example.st7bac_2020e105.R;

public class SettingsActivity extends AppCompatActivity {

    //https://stackoverflow.com/questions/10134338/using-seekbar-to-control-volume-in-android

    private SeekBar volume = null;
    SeekBar radius;
    private AudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        controlVolume();
    }

    private void controlVolume()
    {
        try {
            volume = (SeekBar)findViewById(R.id.seekBar_volume_settingsactivity);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}