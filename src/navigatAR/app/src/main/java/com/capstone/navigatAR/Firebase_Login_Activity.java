package com.capstone.navigatAR;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Firebase_Login_Activity extends AppCompatActivity{

    private Button Button_btn; // 코드를 전달하는 Button
    private EditText EditText_Email;
    private EditText EditText_Password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
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
                String email = EditText_Email.getText().toString();
                String password = EditText_Password.getText().toString();
                loginEvent(email,password);
            }
        });
        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){

                }else{

                }
            }
        };
    }
    void loginEvent(String Email, String Password){
        firebaseAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    //로그인 실패
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(Firebase_Login_Activity.this,"존재하지 않는 id 입니다." ,Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(Firebase_Login_Activity.this,"이메일 형식이 맞지 않습니다." ,Toast.LENGTH_SHORT).show();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(Firebase_Login_Activity.this,"Firebase NetworkException" ,Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Firebase_Login_Activity.this, task.getException().toString() ,Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    //로그인 성공
                    currentUser = firebaseAuth.getCurrentUser();
                    startActivity(new Intent(Firebase_Login_Activity.this,MainActivity.class));
                    Firebase_Login_Activity.this.finish();
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


