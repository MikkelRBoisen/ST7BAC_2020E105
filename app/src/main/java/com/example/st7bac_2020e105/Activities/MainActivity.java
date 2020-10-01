package com.example.st7bac_2020e105.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.st7bac_2020e105.R;

public class MainActivity extends AppCompatActivity {

    Button test1;
    Button test2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test1 = (Button)findViewById(R.id.btnTest1);
        test2 = (Button)findViewById(R.id.btnTest2);


        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));

            }
        });


        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            }
        });

    }
}