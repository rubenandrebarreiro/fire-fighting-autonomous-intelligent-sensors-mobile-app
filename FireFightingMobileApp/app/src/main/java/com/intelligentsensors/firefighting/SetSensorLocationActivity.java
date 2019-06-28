package com.intelligentsensors.firefighting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.intelligentsensors.firefighting.R;
import com.intelligentsensors.firefighting.Sensors.Sensor;
import com.intelligentsensors.firefighting.util.ObjectWrapperForBinder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SetSensorLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String id;

    AsyncHttpClient client;

    private EditText northText;
    private EditText westText;
    private Button confirmButton;
    private Button getLocationButton;

    private GoogleMap map;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sensor_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        id = (String) ((ObjectWrapperForBinder) getIntent().getExtras().getBinder("id")).getData();

        client = new AsyncHttpClient();

        northText = findViewById(R.id.contentSetSensorLocationNorthBox);
        westText = findViewById(R.id.contentSetSensorLocationWestBox);
        confirmButton = findViewById(R.id.contentSetSensorLocationConfirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSetLocationRequest(id, northText.getText().toString(), westText.getText().toString());
            }
        });
        getLocationButton = findViewById(R.id.contentSetSensorLocationCurrLocationButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setEditText(location.getLatitude(),location.getLongitude());
                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                marker.setPosition(latlng);
                cameraZoom(latlng);
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
        final Looper looper = null;

        // Now whenever the button is clicked fetch the location one time
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(SetSensorLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }
        });

    }

    private void sendSetLocationRequest(String id, String north, String west){

        client.addHeader("Accept", "application/json");
        JSONObject jsonParams = new JSONObject();
        try {
            System.out.println("id: " + id);
            System.out.println("north: " + north);
            System.out.println("west: " + west);

            jsonParams.put("sensorId", id);
            jsonParams.put("latlon", north+","+west);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(this, getString(R.string.system_url) + getString(R.string.system_url_setSensorLocation), entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                makeToast(new String(responseBody));
                System.out.println(new String(responseBody));
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                makeToast(new String(responseBody));
                System.out.println(new String(responseBody));
            }
        });
    }

    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latlng = new LatLng(38.661100, -9.203627);
        MarkerOptions aux = new MarkerOptions()
                .position(latlng)
                .draggable(true);

        setEditText(latlng.latitude, latlng.longitude);

        marker = map.addMarker(aux);

        cameraZoom(latlng);

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                northText.setText(String.valueOf(marker.getPosition().latitude));
                westText.setText(String.valueOf(marker.getPosition().longitude));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                onMarkerDrag(marker);
            }
        });
    }

    private void setEditText(double north, double west){
        northText.setText(String.valueOf(north));
        westText.setText(String.valueOf(west));
    }

    private void cameraZoom(LatLng latlng){
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latlng, 16.0f);
        map.animateCamera(cu);
    }
}
