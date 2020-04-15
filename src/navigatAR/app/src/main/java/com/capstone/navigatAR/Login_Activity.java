package com.capstone.navigatAR;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class Login_Activity extends AppCompatActivity {

    LinearLayout LinearLayout_login;
    TextInputEditText TextInputEditText_code;

    String code_OK="AR001";
    String input_code="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText_code = findViewById(R.id.TextInputEditText_code);
        LinearLayout_login = findViewById(R.id.LinearLayout_login);


        //1. 값을 가져온다 - 검사(AR001이여만 통과 서버 연결하면 상관없음.)
        //2. 클릭을 감지한다.
        //3. 1번의 값을 다음 액티비티로 넘긴다.

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
                Intent intent = new Intent(Login_Activity.this, NavigationActivity.class);
                intent.putExtra("code",code);
                startActivity(intent);
           }
        });
    }

    public boolean validation(){
        return input_code.equals(code_OK);
    }
}
