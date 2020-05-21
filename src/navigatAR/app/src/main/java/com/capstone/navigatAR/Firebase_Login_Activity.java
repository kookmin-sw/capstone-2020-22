package com.capstone.navigatAR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Firebase_Login_Activity extends AppCompatActivity{

    private Button Button_btn; // 코드를 전달하는 Button
    private EditText EditText_Email;
    private EditText EditText_Password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_login);
        EditText_Password = (EditText)findViewById(R.id.EditText_password);
        EditText_Email = (EditText)findViewById(R.id.EditText_Email);
        Button_btn = (Button)findViewById(R.id.Button_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        Button_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        });
        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){
                    Intent intent= new Intent(Firebase_Login_Activity.this,MainActivity.class);
                    startActivity(intent);
                    Firebase_Login_Activity.this.finish();
                }else{

                }
            }
        };
    }
    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(EditText_Email.getText().toString(),EditText_Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    //로그인 실패
                    Toast.makeText(Firebase_Login_Activity.this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}


