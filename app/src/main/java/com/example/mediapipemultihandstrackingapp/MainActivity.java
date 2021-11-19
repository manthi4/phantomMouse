package com.example.mediapipemultihandstrackingapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Fragment selectedFragment = new Sc1class();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameL,
                selectedFragment).commit();
    }

}