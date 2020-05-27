package com.capstone.navigatAR;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.WearableArchitectView;
import com.wikitude.common.camera.CameraSettings;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private WearableArchitectView architectView;
    private TextView timeText;
    private TextView distanceText;
    private Location location;
    private DatabaseReference glassRef;
    private int glassNum;
    private String glassKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.architectView = (WearableArchitectView)findViewById(R.id.architectView);
        loadArchitectView();
        glassNum = 1;
        timeText = (TextView)findViewById(R.id.timeText);
        distanceText = (TextView)findViewById(R.id.distanceText);


        database.getReference("users/").orderByChild("num").equalTo(glassNum).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot datas: dataSnapshot.getChildren()) {
                    glassKey = datas.getKey();
                    Log.e(TAG,"glassKey is " + glassKey);
                    glassRef = database.getReference("users/").child(glassKey);
                }
                glassRef.child("distance").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(double.class) != null){
                            double value = dataSnapshot.getValue(double.class);
                            Log.e(TAG,"distance is " + value);
                            distanceText.setText("남은 거리 : " + String.valueOf(value) + "m");
                        }else{
                            Toast.makeText(MainActivity.this, "데이터 없음", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
                    }
                });
                glassRef.child("time").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(int.class) != null){
                            int value = dataSnapshot.getValue(int.class);
                            Log.e(TAG,"time is " + value);
                            timeText.setText("남은 시간 : " + String.valueOf(value) + "분");
                        }else{
                            Toast.makeText(MainActivity.this, "데이터 없음", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        architectView.onDestroy();
    }

    private void setFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                        View.SYSTEM_UI_FLAG_LOW_PROFILE|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    private void loadArchitectView() {
        final ArchitectStartupConfiguration configuration = new ArchitectStartupConfiguration();
        configuration.setLicenseKey(getString(R.string.wikitude_license_key));
        configuration.setCameraPosition(CameraSettings.CameraPosition.DEFAULT);
        configuration.setCameraResolution(CameraSettings.CameraResolution.HD_1280x720);
        configuration.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
        this.architectView.onCreate(configuration);

        this.architectView.onPostCreate();
        try {
            this.architectView.load("ArGeo/index.html");
        }catch (IOException e){

        }

//        this.architectView.callJavascript("loadLogin()");
//        this.architectView.callJavascript("loadWelcome()");
    }
}
