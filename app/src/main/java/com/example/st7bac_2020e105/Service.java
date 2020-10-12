package com.example.st7bac_2020e105;


import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Service extends android.app.Service {

    private final IBinder mBinder = new LocalBinder();


    //https://www.youtube.com/watch?v=p2ffzsCqrs8&ab_channel=SimplifiedCoding

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private DocumentReference mRef;

    public void Service(){}

    public class LocalBinder extends Binder {
        public Service getService() {
            // Return this instance of LocalService so clients can call public methods
            return Service.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mRef = firestore.collection("").document();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
    //    Alarm alarm = new Alarm();
       return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
