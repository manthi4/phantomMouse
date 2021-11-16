package com.google.mediapipe.apps.basic;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
// import com.google.mediapipe.apps.basic.R;
// import android.R;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BleMouse mouse = new BleMouse(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mouse.getProxy();
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameL,
                    new Sc1class()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if(item.getItemId() == R.id.nav_bluetooth){
                        selectedFragment = new Sc1class();
                    }
                    if(item.getItemId() ==  R.id.nav_callibrate){
                        selectedFragment = new Sc2class();
                    }
                    if(item.getItemId() == R.id.nav_settings){
                        selectedFragment = new Sc3class(mouse);
                        // selectedFragment.setMouse(mouse);
                    }
                    // switch (item.getItemId()) {
                    //     case R.id.nav_bluetooth:
                    //         selectedFragment = new Sc1class();
                    //         break;
                    //     case R.id.nav_callibrate:
                    //         selectedFragment = new Sc2class();
                    //         break;
                    //     case R.id.nav_settings:
                    //         selectedFragment = new Sc3class(mouse);
                    //         break;
                    // }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameL,
                            selectedFragment).commit();
                    return true;
                }
            };
}