package com.bcit.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button goToRPS = (Button) findViewById(R.id.buttonRPS);
        goToRPS.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RPS.class);
                startActivity(i);
            }
        });

        final Button goToUDP = (Button) findViewById(R.id.buttonUDP);
        goToUDP.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), UDP.class);
                startActivity(i);
            }
        });
    }
}