package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.st7bac_2020e105.R;

public class MainActivity extends AppCompatActivity {

    Button testActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testActivities = (Button)findViewById(R.id.testActivitiesButton);
        testActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class); //ÆNDRER ACTIVITES FOR AT TESTE!
                MainActivity.this.startActivity(myIntent);
            }
        });

    }
}