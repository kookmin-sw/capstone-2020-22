package com.capstone.navigatAR;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Loading_Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
             setContentView(R.layout.activity_loading);
        try {
            Thread.sleep(2000); //대기 시간 설정
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        //2000 대기 후 로그인 액티비티 실행
        startActivity(new Intent(this, Login_Activity.class));
        finish();
    }
}


