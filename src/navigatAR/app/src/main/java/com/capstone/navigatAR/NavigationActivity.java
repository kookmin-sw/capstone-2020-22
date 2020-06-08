package com.capstone.navigatAR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
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
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class NavigationActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private static final String TAG = NavigationActivity.class.getSimpleName();
    private long INTERVAL_IN_MILLISECONDS = 1000L;
    private long MAX_WAIT_TIME = INTERVAL_IN_MILLISECONDS * 5;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

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
    public static double lat, lng;
    private Marker mapMarker;
    private Button startButton;
    private Button infoButton;

    private int time;
    private double distance;
    private TextView remainText;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private String userID;
    DatabaseReference glassRef;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e(TAG, "NavigationActivity onCreate 실행");
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token)); // mapbox api 토큰 받아오기
        setContentView(R.layout.nav_layout);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        database = FirebaseDatabase.getInstance();
        glassRef = database.getReference("users/" + userID); //로그인한 사용자의 DB 노드를 glassRef로 담음.

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

                NavigationLauncher.startNavigation(NavigationActivity.this, options);
                //네비게이션 실행
            }
        });
        infoButton = findViewById(R.id.InfoButton);
        infoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(NavigationActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
        ImageButton myLoc_button = findViewById(R.id.myLoc_button); // 내 위치로 가는 버튼
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

        remainText = (TextView)findViewById(R.id.remainText);

    }
    // onCreate 끝

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Log.e(TAG, "onMapReady");

        NavigationActivity.this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(NavigationActivity.this);
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/kooym/ck82rmy5h28js1ioa295dzgc7"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initSearchFab();
                enableLocationComponent(style);
                LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                try {
                    localizationPlugin.matchMapLanguageWithDeviceDefault();
                } catch (RuntimeException exception) {
                    Log.d(TAG, exception.toString());
                }
                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);
            }
        });

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point){
        Log.e(TAG,"onMapClick 실행");

        if(mapMarker!=null) //1개의 마커만 표시.
            mapboxMap.removeMarker(mapMarker);

        mapMarker = mapboxMap.addMarker(new MarkerOptions().position(point));
        destinationPos = Point.fromLngLat(point.getLongitude(), point.getLatitude()); //클릭한 곳의 위도와 경도를 Point 형식으로 destinationPos에 담음

        if(glassRef != null) {
            glassRef.child("destination").child("latitude").setValue(destinationPos.latitude());
            glassRef.child("destination").child("longitude").setValue(destinationPos.longitude());
        } // 사용자 db노드 하위에 destination으로 그 하위에는 위도와 경도를 저장.

        showSearchItem(myPos, destinationPos);

        return false;
    }

    private void initSearchFab() { //자동검색창 띄우기
        findViewById(R.id.location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(8)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(NavigationActivity.this);
                startActivityForResult(intent, 1);
            }
        });
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
                            Log.e(TAG,"다이얼로그에서 도보 선택");
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
                            Log.e(TAG,"다이얼로그에서 자전거 선택");
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

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == 1){
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if(mapboxMap!=null){
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if(source!=null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                }
                startButton.setEnabled(true); // 목적지가 없으면 버튼이 비활성화, 목적지가 선택되었으니 버튼 활성화.

                LatLng desPos = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                        ((Point) selectedCarmenFeature.geometry()).longitude()); //검색어를 클릭할 시 그 위치의 위도 경도를 desPos에 저장.

                if(mapMarker!=null) //1개의 마커만 표시.
                    mapboxMap.removeMarker(mapMarker);
                mapMarker = mapboxMap.addMarker(new MarkerOptions().position(desPos));

                destinationPos = Point.fromLngLat(desPos.getLongitude(), desPos.getLatitude());
                myPos = Point.fromLngLat(lng,lat);
                showSearchItem(myPos,destinationPos);
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                        ((Point) selectedCarmenFeature.geometry()).longitude()))
                                .zoom(14)
                                .build()), 4000);
            }
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
        }
        else {
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
    }


    private void getRoute(Point origin, Point destination,int profile) { // 지도상 경로 받아오기
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
                distance = (currentRoute.distance()/1000);
                //목적지까지의 거리를 m로 받아옴

                distance = Math.round(distance*100)/100.0;
                //Math.round() 함수는 소수점 첫째자리에서 반올림하여 정수로 남긴다
                //원래 수에 100곱하고 round 실행 후 다시 100으로 나눈다 -> 둘째자리까지 남김

                Toast.makeText(getApplicationContext(), String.format("예상 시간 : " + String.valueOf(time)+" 분 \n" +
                        "목적지 거리 : " +distance+ " km"), Toast.LENGTH_LONG).show();
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

    private void getRoute_navi (Point origin, Point destinaton, int profile) { //선택한 방법대로 네비게이션 정보 넣기
        Log.e(TAG,"getRoute_navi 실행");

        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .profile(profile==1?DirectionsCriteria.PROFILE_WALKING:DirectionsCriteria.PROFILE_CYCLING)//도보 길찾기
                .origin(origin)//출발지
                .destination(destinaton)//도착지
                .language(Locale.forLanguageTag("Korean"))
                .build().
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
                myPos = Point.fromLngLat(lng,lat);
                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
                if(destinationPos!=null && currentRoute!=null && currentRoute.distance()!=null){ //실시간 남은 거리 및 시간
                    time = (int) (currentRoute.duration()/60);
                    //예상 시간을초단위로 받아옴
                    distance = (currentRoute.distance()/1000);
                    //목적지까지의 거리를 m로 받아옴
                    distance = Math.round(distance*100)/100.0;
                    remainText.setText("남은 시간 : " + String.valueOf(time) + "분\n남은 거리 : " + String.valueOf(distance) + "km");

                    // 시간과 거리를 db에 저장
                    if(glassRef != null) {
                        glassRef.child("time").setValue(time);
                        glassRef.child("distance").setValue(distance);
                        glassRef.child("Location").setValue(location);
                    }
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