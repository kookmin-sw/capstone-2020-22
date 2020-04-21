package com.capstone.navigatAR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import static com.mapbox.mapboxsdk.LibraryLoader.load;

public class Login_Activity extends AppCompatActivity implements View.OnClickListener {

    Button Button_btn; // 코드를 전달하는 Button
    TextInputEditText TextInputEditText_code; //코드 받는 EditText
    CheckBox CheckBox_auto_login;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText_code = findViewById(R.id.TextInputEditText_code);
        Button_btn = findViewById(R.id.Button_btn);
        CheckBox_auto_login = findViewById(R.id.CheckBox_auto_login);

        sharedPreferences = getSharedPreferences("LoginData",0);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("CheckBox_auto_login",false)){
            TextInputEditText_code.setText(sharedPreferences.getString("CODE",""));
            CheckBox_auto_login.setChecked(true);
        }
        Button_btn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        if(CheckBox_auto_login.isChecked()){
            Toast.makeText(this, "로그인", Toast.LENGTH_SHORT).show();
            String CODE = TextInputEditText_code.getText().toString();
            editor.putString("CODE", CODE);
            editor.putBoolean("CheckBox_auto_login", true);
            editor.commit();
            Intent intent = new Intent(Login_Activity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            editor.clear();
            editor.commit();
        }
    }
}
