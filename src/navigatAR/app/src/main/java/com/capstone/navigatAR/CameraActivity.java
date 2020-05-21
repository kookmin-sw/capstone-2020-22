package com.capstone.navigatAR;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.navigatAR.util.SampleData;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    protected ArchitectView architectView;
    private String arExperience;

    private static final String TAG = CameraActivity.class.getSimpleName();
    public static final String INTENT_EXTRAS_KEY_SAMPLE = "sampleData";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        final SampleData sampleData = (SampleData) intent.getSerializableExtra(INTENT_EXTRAS_KEY_SAMPLE);
//        arExperience = sampleData.getPath();

        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(getString(R.string.wikitude_license_key));
//        config.setCameraPosition(sampleData.getCameraPosition());
//        config.setCameraResolution(sampleData.getCameraResolution());
//        config.setCameraFocusMode(sampleData.getCameraFocusMode());
//        config.setCamera2Enabled(sampleData.isCamera2Enabled());

        architectView = (ArchitectView)this.findViewById( R.id.architectView );
        architectView = new ArchitectView(this);
        architectView.onCreate(config);
        setContentView(architectView);
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        architectView.onPostCreate();

        try {
            /*
             * Loads the AR-Experience, it may be a relative path from assets,
             * an absolute path (file://) or a server url.
             *
             * To get notified once the AR-Experience is fully loaded,
             * an ArchitectWorldLoadedListener can be registered.
             */
            architectView.load("samples/" + arExperience);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_loading_ar_experience), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience " + arExperience + ".", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Deletes all cached files of this instance of the ArchitectView.
         * This guarantees that internal storage for this instance of the ArchitectView
         * is cleaned and app-memory does not grow each session.
         *
         * This should be called before architectView.onDestroy
         */
        architectView.clearCache();
        architectView.onDestroy(); // Mandatory ArchitectView lifecycle call
    }
}