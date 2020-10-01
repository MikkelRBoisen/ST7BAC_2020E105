package com.example.st7bac_2020e105;


import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class Service extends android.app.Service {

    private Alarm alarm = new Alarm();
    //https://www.youtube.com/watch?v=p2ffzsCqrs8&ab_channel=SimplifiedCoding

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
    //    Alarm alarm = new Alarm();
        alarm.playAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarm.stopAlarm();

    }
}
