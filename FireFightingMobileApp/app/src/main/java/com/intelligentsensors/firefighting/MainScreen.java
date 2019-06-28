package com.intelligentsensors.firefighting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligentsensors.firefighting.Sensors.Sensor;
import com.intelligentsensors.firefighting.util.CurrentUser;
import com.intelligentsensors.firefighting.util.ObjectWrapperForBinder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private AsyncHttpClient client;
    private GoogleMap mMap;
    private List<Marker> sensors;
    private List<Marker> users;
    private CurrentUser currentUser;

    private static final int TEMPERATURE_OK = 25;
    private static final int TEMPERATURE_YELLOW = 35;
    private static final int TEMPERATURE_ORANGE = 45;
    private static final int GPS_MIN_TIME = 5000;
    private static final int GPS_MIN_DISTANCE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        client = new AsyncHttpClient();
        sensors = new ArrayList<>();
        users = new ArrayList<>();
        currentUser = (CurrentUser) getApplication();
        getCurrentUserDetails();

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(currentUser.isLoggedIn()){
                    try{
                        System.out.println("Location changed to " + location.getLatitude() + "," + location.getLongitude());
//                        Toast.makeText(getApplication(), "Location changed to " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
                        JSONObject json = new JSONObject();
                        json.put("email", currentUser.getCurrUserEmail());
                        json.put("latlon", location.getLatitude()+","+location.getLongitude());
                        StringEntity entity = new StringEntity(json.toString());
                        client.post(getApplication(),
                                getString(R.string.system_url) + getString(R.string.system_url_user_updateLoc),
                                entity,
                                "application/json",
                                new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        System.out.println("Location of user " + currentUser.getCurrUserUserName() + " successfully changed");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Toast.makeText(getApplication(), "Location change failed", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Now first make a criteria with your requirements
        // this is done to save the battery life of the device
        // there are various other other criteria you can search for..
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = getMainLooper();

        // Now whenever the button is clicked fetch the location one time
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainScreen.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestSingleUpdate(criteria, locationListener, looper);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                GPS_MIN_TIME,
                GPS_MIN_DISTANCE,
                locationListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getCurrentUserDetails() {
        client.get(getString(R.string.system_url) + getString(R.string.system_url_getUsers), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                System.out.println("Success");

                try {
                    System.out.println("Registered users\n" + responseBody);
                    JSONObject propertyMap;
                    JSONObject json;
                    String name;
                    String email;
                    String adminAux;
                    boolean admin = false;
                    for (int i = 0; i < responseBody.length(); i++) {
                        json = responseBody.getJSONObject(i);
                        propertyMap = json.getJSONObject("propertyMap");
                        name = propertyMap.getString("user_name");
                        email = propertyMap.getString("user_mail");
                        if (email.equals(currentUser.getCurrUserEmail())){
                            adminAux = propertyMap.getString("user_type");
                            if (adminAux.equalsIgnoreCase(getString(R.string.general_admin)))
                                admin = true;
                            currentUser.setNewLoggedInUser(currentUser.getCurrUserEmail(),
                                    name, admin);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                System.err.println("Error");
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Object tag = marker.getTag();
                if(tag instanceof Sensor)
                    openDetails((Sensor)marker.getTag());
                else Toast.makeText(getApplication(), "Can only show sensors", Toast.LENGTH_LONG)
                        .show();
            }
        });

        //InfoWindow creation so all of the snippet appears
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                final Context mContext = getApplicationContext();

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView position = new TextView(mContext);
                position.setTextColor(Color.BLACK);
                position.setText("N: " + marker.getPosition().latitude +
                        "\nW: " + marker.getPosition().longitude);

                TextView seeMore = new TextView(mContext);
                seeMore.setTextColor(Color.GRAY);
                seeMore.setText("Click here to see more details");

                info.addView(title);
                info.addView(position);
                info.addView(seeMore);

                return info;
            }
        });
        getSensors(true);
    }

    private void getUsers(final boolean setCameraZoom){

        client.get(getString(R.string.system_url) + getString(R.string.system_url_getUsers), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                System.out.println("Success");

                try {
                    System.out.println("Registered users\n"+ responseBody);
                    JSONObject propertyMap;
                    JSONObject json;
                    String name;
                    String[] latlon;
                    double N;
                    double W;
                    for (int i = 0; i < responseBody.length(); i++) {
                        json = responseBody.getJSONObject(i);
                        propertyMap = json.getJSONObject("propertyMap");
                        name = propertyMap.getString("user_name");
                        latlon = propertyMap.getString("latlon").split(",");
                        N = Double.parseDouble(latlon[0]);
                        W = Double.parseDouble(latlon[1]);
                        users.add(mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(N,W))
                                .title(name)
                                .snippet("N: " + N + '\n' + "W: " + W)
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.user_marker_1_small)))));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(setCameraZoom)
                    setAllCameraZoom();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                System.err.println("Error");
                if (setCameraZoom)
                    setAllCameraZoom();
            }
        });

    }

    private void getSensors(final boolean setCameraZoom){

        client.get(getString(R.string.system_url) + getString(R.string.system_url_getSensorsWithLocation), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                System.out.println("Success");

                try {
                    System.out.println("Registered sensors\n"+ responseBody);
                    JSONObject key;
                    JSONObject propertyMap;
                    JSONObject json;
                    String name;
                    String[] latlon;
                    String N;
                    String W;
                    String sensorRegTime;
                    String dateModified;
                    String temperature;
                    String humidity;
                    for (int i = 0; i < responseBody.length(); i++) {
                        json = responseBody.getJSONObject(i);
                        key = json.getJSONObject("key");
                        name = key.getString("name");
                        propertyMap = json.getJSONObject("propertyMap");
                        try{
                            latlon = propertyMap.getString("latlon").split(",");
                            N = latlon[0];
                            W = latlon[1];
                        } catch (JSONException e) {
                            System.err.println("No latlon");
                            N = null;
                            W = null;
                        }

                        sensorRegTime = propertyMap.getString("sensor_reg_time");

                        try{
                            dateModified = propertyMap.getString("date_modified");
                        } catch (JSONException e) {
                            System.err.println("No date_modified");
                            dateModified = null;
                        }
                        try{
                            temperature = propertyMap.getString("temperature");
                        } catch (JSONException e) {
                            System.err.println("No temperature");
                            temperature = null;
                        }

                        try {
                            humidity = propertyMap.getString("humidity");
                            System.err.println("No humidity");
                        } catch (JSONException e) {
                            humidity = null;
                        }
                        BitmapDescriptor factory = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.idle_marker_1_small));
                        if(temperature != null){
                            int auxTemp = Integer.parseInt(temperature);
                            if(auxTemp < TEMPERATURE_OK)
                                factory = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.idle_marker_1_small));
                            else if(auxTemp < TEMPERATURE_YELLOW)
                                factory = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.yellow_alert_marker_1_small));
                            else if(auxTemp < TEMPERATURE_ORANGE)
                                factory = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.orange_alert_marker_1_small));
                            else factory = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.fire_alert_marker_1_small));
                        }
                        LatLng auxLatlon = new LatLng(Double.parseDouble(N),Double.parseDouble(W));
                        sensors.add(mMap.addMarker(new MarkerOptions()
                                .position(auxLatlon)
                                .title(name)
                                .snippet("N: " + N + '\n' + "W: " + W)
                                .icon(factory)));
                        sensors.get(sensors.size() - 1).setTag(new Sensor(name, auxLatlon,
                                sensorRegTime, dateModified, temperature, humidity));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getUsers(setCameraZoom);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                System.err.println("Error");
                getUsers(setCameraZoom);
            }
        });

    }

    private void setSensorsCameraZoom(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : sensors) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    private void setUsersCameraZoom(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : users) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    private void setAllCameraZoom(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : users) {
            builder.include(marker.getPosition());
        }
        for (Marker marker : sensors) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    private void openDetails(Sensor sensor){
        Intent intent = new Intent(this,LocationDetails.class);
        Bundle bundle = new Bundle();
        bundle.putBinder("sensor", new ObjectWrapperForBinder(sensor));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void refresh(boolean setCameraZoom){
        if(mMap != null){
            mMap.clear();
            sensors = new ArrayList<>();
            users = new ArrayList<>();
            getSensors(setCameraZoom);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up getButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_all) {
            setAllCameraZoom();
        }
        else if(id == R.id.action_show_sensors){
            setSensorsCameraZoom();
        } else if(id == R.id.action_show_users){
            setUsersCameraZoom();
        } else if(id == R.id.action_refresh){
            refresh(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        if (id == R.id.nav_sensors) {
            startActivity(intent.setClass(this, ActorActivity.class));
        } else if (id == R.id.nav_map) {
            startActivity(intent.setClass(this, MainScreen.class));
        } else if (id == R.id.nav_logout){
            CurrentUser currentUser = (CurrentUser)getApplication();
            Toast.makeText(this, "Logging out " + currentUser.getCurrUserUserName(), Toast.LENGTH_LONG).show();
            currentUser.logout();
            startActivity(intent.setClass(this, MainActivity.class));
        }
        fragmentTransaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        refresh(false);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

}
