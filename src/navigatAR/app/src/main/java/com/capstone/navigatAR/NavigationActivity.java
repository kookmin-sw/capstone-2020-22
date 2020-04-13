package com.capstone.navigatAR;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;

public class NavigationActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private static final String TAG = NavigationActivity.class.getSimpleName();
    private long INTERVAL_IN_MILLISECONDS = 1000L;
    private long MAX_WAIT_TIME = INTERVAL_IN_MILLISECONDS * 5;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private NavigationActivityLocationCallback callback = new NavigationActivityLocationCallback(this);
    private MapboxDirections client;
    private static DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    private Point myPos;
    private Point destinationPos;
    double destinationLng,destinationLat;
    public static double lat, lng;
    private Marker clickMarker;
    private Button startButton;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e(TAG, "NavigationActivity onCreate 실행");
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token)); // mapbox api 토큰 받아오기
        setContentView(R.layout.nav_layout);

        mapView = findViewById(R.id.mapView); //mapbox의 지도 표현
        mapView.onCreate(savedInstanceState);
        Log.e(TAG,"mapview onCreate 실행");
        mapView.getMapAsync(this);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startButton.setEnabled(false);

                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .build();
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(NavigationActivity.this, options);
                //네비게이션 실행 (MainActivity에서)
            }
        });

        Button myLoc_button = findViewById(R.id.myLoc_button); // 내 위치로 가는 버튼
        myLoc_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng)) // Sets the new camera position
                        .zoom(17) //  줌 정도 숫자가 클수록 더많이 줌함
                        .bearing(0) // Rotate the camera , 카메라 방향(북쪽이 0) 북쪽부터 시계방향으로 측정
                        .tilt(0) // Set the camera tilt , 각도
                        .build(); // Creates a CameraPosition from the builder
                //카메라 움직이기
                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);
                Toast.makeText(getApplicationContext(), String.format("            내위치 \n위도 : " + lat + "\n경도 : "+ lng), Toast.LENGTH_SHORT).show();
            }
        });
    }
// onCreate 끝

    @Override
    public boolean onMapClick(@NonNull LatLng point){
        Log.e(TAG,"onMapClick 실행");

        if(clickMarker!=null) //1개의 마커만 표시.
            mapboxMap.removeMarker(clickMarker);

        clickMarker = mapboxMap.addMarker(new MarkerOptions().position(point));
        destinationPos = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        myPos = Point.fromLngLat(lng,lat);
        showSearchItem(myPos, destinationPos);

        return false;
    }

    public void showSearchItem(Point origin, Point destination){  // 가는 방법 고르는 다이얼로그
        Log.e(TAG,"showSearchItem 실행");
        Handler mHandler = new Handler();
        final CharSequence[] oItems = {"도보", "자전거"};
        AlertDialog.Builder oDialog = new AlertDialog.Builder(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        oDialog.setTitle("방법을 선택하세요")
                .setItems(oItems, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (which == 0 ) {
                            //도보 길찾기 진행
                            getRoute(origin,destination,1);//예상 시간 및 위도 경도 출력
                            getRoute_navi(origin,destination,1);//네비게이션 정보 저장
                            mHandler.postDelayed(new Runnable(){ // start버튼 활성화. 1초의 딜레이를 두어 에러 나는거 발생.
                                public void run(){
                                    startButton.setEnabled(true);
                                }
                            }, 1000);
                        }
                        else if ( which == 1) {
                            //자전거 길찾기 진행
                            getRoute(origin,destination,2);//예상 시간 및 위도 경도 출력
                            getRoute_navi(origin,destination,2);//네비게이션 정보 저장
                            mHandler.postDelayed(new Runnable(){ // start버튼 활성화. 1초의 딜레이를 두어 에러 나는거 발생.
                                public void run(){
                                    startButton.setEnabled(true);
                                }
                            }, 1000);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setCancelable(false) //뒤로가기로 취소 막기
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){
            if(resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                editText.setText(result.get(0));

                startButton.setEnabled(true);
                getPointFromGeoCoder(editText.getText().toString());
                Point origin = Point.fromLngLat(lng,lat);
                Point destination = Point.fromLngLat(destinationLng, destinationLat);
                getRoute(origin,destination,1);//폴리라인 그리기
                getRoute_navi(origin,destination,1);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Log.e(TAG, "onMapReady");

        NavigationActivity.this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(NavigationActivity.this);
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/kooym/ck82rmy5h28js1ioa295dzgc7"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });
    }

    public void getPointFromGeoCoder(String destinationxy) {
        Log.e(TAG,"지오코더 실행");
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(destinationxy, 1);
            destinationLng = listAddress.get(0).getLongitude();
            destinationLat = listAddress.get(0).getLatitude();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() { // 실시간 위치 정보 업데이트
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = new LocationEngineRequest.Builder(INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private void drawRoute(DirectionsRoute route) { //지오코딩된 내용을 바탕으로 포인트에 값 저장
        Log.e(TAG,"drawRoute 실행");
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
        List<Point> coordinates = lineString.coordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(coordinates.get(i).latitude(), coordinates.get(i).longitude());

            Log.e(TAG, "Error: " + points[i]);
        }
        // Draw Points on MapView
//        mapboxMap.clear();
//      mapboxMap.addPolyline(new PolylineOptions().add(points).color(Color.parseColor("#3bb2d0")).width(5));
    }


    private void getRoute(Point origin, Point destination,int profile) {
        Log.e(TAG,"getRoute 실행");
        client = MapboxDirections.builder()
                .origin(origin)//출발지 위도 경도
                .destination(destination)//도착지 위도 경도
                .overview(DirectionsCriteria.OVERVIEW_FULL)//정보 받는정도 최대
                .profile(profile==1?DirectionsCriteria.PROFILE_WALKING:DirectionsCriteria.PROFILE_CYCLING)//길찾기 방법(도보,자전거,자동차)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.e(TAG,"onResponse 실행");
                System.out.println(call.request().url().toString());
                Log.e(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                // Print some info about the route
                currentRoute = response.body().routes().get(0);
                Log.e(TAG, "Distance: " + currentRoute.distance());

                int time = (int) (currentRoute.duration()/60);
                //예상 시간을초단위로 받아옴
                double distants = (currentRoute.distance()/1000);
                //목적지까지의 거리를 m로 받아옴

                distants = Math.round(distants*100)/100.0;
                //Math.round() 함수는 소수점 첫째자리에서 반올림하여 정수로 남긴다
                //원래 수에 100곱하고 round 실행 후 다시 100으로 나눈다 -> 둘째자리까지 남김

                Toast.makeText(getApplicationContext(), String.format("예상 시간 : " + String.valueOf(time)+" 분 \n" +
                        "목적지 거리 : " +distants+ " km"), Toast.LENGTH_LONG).show();
                // Draw the route on the map
                drawRoute(currentRoute);

            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(NavigationActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRoute_navi (Point origin, Point destinaton, int profile) {
        Log.e(TAG,"getRoute_navi 실행");

        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .profile(profile==1?DirectionsCriteria.PROFILE_WALKING:DirectionsCriteria.PROFILE_CYCLING)//도보 길찾기
                .origin(origin)//출발지
                .destination(destinaton).//도착지
                build().
                getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            return;
                        } else if (response.body().routes().size() ==0) {
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        if (navigationMapRoute != null) { // 경로를 하나만 지정.
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    }
                });
    }


    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationEngine!=null){
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    class NavigationActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<NavigationActivity> activityWeakReference;
        NavigationActivityLocationCallback(NavigationActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(LocationEngineResult result) {
            NavigationActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                lat = result.getLastLocation().getLatitude();
                lng = result.getLastLocation().getLongitude();
                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }


        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            NavigationActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "위치 권한 승인주세요.", Toast.LENGTH_LONG).show();
    }
}
