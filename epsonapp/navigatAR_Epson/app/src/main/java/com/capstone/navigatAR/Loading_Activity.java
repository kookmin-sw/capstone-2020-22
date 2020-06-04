package com.capstone.navigatAR;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Loading_Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);
        Handler hd= new Handler();
        hd.postDelayed(new SplashHandler(),1500);
    }

    private class SplashHandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(),MainActivity.class));
            Loading_Activity.this.finish();
        }
    }
    @Override
    public void onBackPressed(){
    }
}


