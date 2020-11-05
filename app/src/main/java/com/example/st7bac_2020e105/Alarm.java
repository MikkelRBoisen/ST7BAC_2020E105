package com.example.st7bac_2020e105;

import android.content.Context;
import android.media.MediaPlayer;

public class Alarm {

    //https://dzone.com/articles/playing-sounds-android

    MediaPlayer mediaPlayer = null;

    public void playAlarm(Context context)
    {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        //mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void stopAlarm(Context context)
    {
        mediaPlayer.stop();
    }

}
