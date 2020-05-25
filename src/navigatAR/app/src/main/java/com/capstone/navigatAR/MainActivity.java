package com.capstone.navigatAR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;


public class MainActivity extends AppCompatActivity {
    private  Button main_btn;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment_help fragment_help = new Fragment_help();
    private Fragment_home fragment_home = new Fragment_home();
    private Fragment_logout fragment_logout = new Fragment_logout();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        main_btn = findViewById(R.id.main_btn);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_framelayout,fragment_home).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId())
                {
                    case R.id.action1:
                        transaction.replace(R.id.main_framelayout,fragment_logout).commitAllowingStateLoss();
                        break;
                    case R.id.action2:
                        transaction.replace(R.id.main_framelayout,fragment_home).commitAllowingStateLoss();
                        break;
                    case R.id.action3:
                        transaction.replace(R.id.main_framelayout,fragment_help).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });

        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });
    }
}
