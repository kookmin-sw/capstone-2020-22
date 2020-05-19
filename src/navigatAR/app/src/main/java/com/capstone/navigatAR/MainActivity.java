package com.capstone.navigatAR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private  Button main_btn;
    private  Button logout_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_btn = findViewById(R.id.main_btn);
        logout_btn = findViewById(R.id.logout_btn);

        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);
                finish();
            }
        });

    }


   /* public void onButtonNavClick(View v){
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }*/
}
