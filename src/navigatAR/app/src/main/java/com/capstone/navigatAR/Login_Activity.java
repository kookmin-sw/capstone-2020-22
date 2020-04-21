package com.capstone.navigatAR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import static com.mapbox.mapboxsdk.LibraryLoader.load;

public class Login_Activity extends AppCompatActivity {

    LinearLayout LinearLayout_login; // 코드를 전달하는 Button
    TextInputEditText TextInputEditText_code; //코드 받는 EditText
    private  boolean saveLoginData;
    private String code_OK="AR001";
    private String input_code="";
    CheckBox CheckBox_auto_login;
    private SharedPreferences appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //설정값 불러오기
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        load();
        TextInputEditText_code = findViewById(R.id.TextInputEditText_code);
        LinearLayout_login = findViewById(R.id.LinearLayout_login);
        CheckBox_auto_login = findViewById(R.id.CheckBox_auto_login);

        if (saveLoginData) {
            TextInputEditText_code.setText(code_OK);
            CheckBox_auto_login.setChecked(saveLoginData);
        }

        LinearLayout_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 성공시 저장 처리, 예제는 무조건 저장
                save();
            }
        });

        LinearLayout_login.setEnabled(false);
        TextInputEditText_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Log.d("SENTI",s.toString()); //파라미터가 무슨역할을 하는 지 알아보기 위한 로그 디버깅
                input_code=s.toString();
                if(s !=null) {
                    input_code = s.toString();
                    LinearLayout_login.setEnabled(validation());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        LinearLayout_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code=TextInputEditText_code.getText().toString();
                Intent intent = new Intent(Login_Activity.this, MainActivity.class);
                intent.putExtra("code",code);
                startActivity(intent);
                finish();
            }
        });

    }

    public boolean validation(){
        return input_code.equals(code_OK);
    }

    private void save() {
            // SharedPreferences 객체만으론 저장 불가능 Editor 사용
            SharedPreferences.Editor editor = appData.edit();

            // 에디터객체.put타입( 저장시킬 이름, 저장시킬 값 )
            // 저장시킬 이름이 이미 존재하면 덮어씌움
            editor.putBoolean("SAVE_LOGIN_DATA", CheckBox_auto_login.isChecked());
            editor.putString("CODE", TextInputEditText_code.getText().toString().trim());

            // apply, commit 을 안하면 변경된 내용이 저장되지 않음
            editor.apply();
        }

        // 설정값을 불러오는 함수
    private void load() {
            // SharedPreferences 객체.get타입( 저장된 이름, 기본값 )
            // 저장된 이름이 존재하지 않을 시 기본값
            saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false);
            code_OK = appData.getString("CODE", "AR001");
        }
}
