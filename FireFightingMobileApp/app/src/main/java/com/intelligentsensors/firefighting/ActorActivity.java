package com.intelligentsensors.firefighting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligentsensors.firefighting.Sensors.Sensor;
import com.intelligentsensors.firefighting.util.CurrentUser;
import com.intelligentsensors.firefighting.util.ObjectWrapperForBinder;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ActorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AsyncHttpClient client;
    LinearLayout registeredLayout;
    LinearLayout nonRegisteredLayout;
    LinearLayout usersLayout;
    int layoutID = 0;
    CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUI();
        client = new AsyncHttpClient();

        registeredLayout = findViewById(R.id.activityActorsRegisteredList);
        nonRegisteredLayout = findViewById(R.id.activityActorsNonRegisteredList);
        usersLayout = findViewById(R.id.activityActorsUsersList);

        currentUser = (CurrentUser)getApplication();
    }

    private void setUI(){
        setContentView(R.layout.activity_actor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void refresh(){
        setUI();
        getSensors();
    }

    private void getSensors(){
        ViewGroup parent = findViewById(R.id.activityActorsRegisteredList);
        getRemoteSensors(getString(R.string.system_url_getSensorsWithLocation),parent);
        ViewGroup parent2 = findViewById(R.id.activityActorsNonRegisteredList);
        getRemoteSensors(getString(R.string.system_url_getSensorsWithoutLocation), parent2);
        ViewGroup parent3 = findViewById(R.id.activityActorsUsersList);
        getUsers(parent3);
    }

    private void getUsers(final ViewGroup parent){
        client.get(getString(R.string.system_url) + getString(R.string.system_url_getUsers), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                System.out.println("Success");

                try {
                    System.out.println("Registered users\n"+ responseBody);
                    JSONObject propertyMap;
                    JSONObject json;
                    String name;
                    String email;
                    String adminAux;
                    boolean admin = false;
                    String[] latlon;
                    double N;
                    double W;
                    for (int i = 0; i < responseBody.length(); i++) {
                        json = responseBody.getJSONObject(i);
                        propertyMap = json.getJSONObject("propertyMap");
                        name = propertyMap.getString("user_name");
                        email = propertyMap.getString("user_mail");
                        adminAux = propertyMap.getString("user_type");
                        if(adminAux.equalsIgnoreCase(getString(R.string.general_admin)))
                            admin = true;
                        latlon = propertyMap.getString("latlon").split(",");
                        N = Double.parseDouble(latlon[0]);
                        W = Double.parseDouble(latlon[1]);
                        newUserRowLayout(name, String.valueOf(N), String.valueOf(W), admin, email, layoutID++, parent);
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

    private void getRemoteSensors(String url, final ViewGroup parent){
        client.get(getString(R.string.system_url) + url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                try {
                    System.out.println("Registered sensors\n"+responseBody);
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
                        newSensorRowLayout(name, N, W, sensorRegTime, dateModified, temperature, humidity, layoutID++, parent);
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

    private void seeDetails(Sensor sensor){
        System.out.println("Seeing details");
        Intent intent = new Intent(this,LocationDetails.class);
        Bundle bundle = new Bundle();
        bundle.putBinder("sensor", new ObjectWrapperForBinder(sensor));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void newSensorRowLayout(final String inName,
                                    final String inN,
                                    final String inW,
                                    final String sensorRegTime,
                                    final String dateModified,
                                    final String temperature,
                                    final String humidity,
                                    int layoutID,
                                    ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View newRow = inflater.inflate(R.layout.sensor_row, parent, false);
        TextView name = newRow.findViewById(R.id.sensorRowSensorID);
        name.setText(inName);
        Button addInfo = newRow.findViewById(R.id.sensorRowAddInformation);
        addInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSensorInfo(inName);
            }
        });
        if(!currentUser.isAdmin())
            addInfo.setVisibility(View.INVISIBLE);
        TextView N = newRow.findViewById(R.id.sensorRowNCoordinates);
        TextView W = newRow.findViewById(R.id.sensorRowWCoordinates);
        if(inN != null || inW != null) {
            N.setText("N: " + inN);
            W.setText("W: " + inW);
        } else {
            N.setText("");
            W.setText(getString(R.string.actor_list_activity_no_location));
        }

        Button details = newRow.findViewById(R.id.sensorRowMoreDetailsButton);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inN == null || inW == null)
                    seeDetails(new Sensor(inName,
                            sensorRegTime,
                            dateModified,
                            temperature,
                            humidity));
                else
                    seeDetails(new Sensor(inName,
                            new LatLng(Double.parseDouble(inN),Double.parseDouble(inW)),
                            sensorRegTime,
                            dateModified,
                            temperature,
                            humidity));
            }
        });
        newRow.setId(layoutID);
        parent.addView(newRow);
    }

    private void newUserRowLayout(final String inName,
                                  final String inN,
                                  final String inW,
                                  final boolean inAdmin,
                                  final String inEmail,
                                  int layoutID,
                                  ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View newRow = inflater.inflate(R.layout.user_row, parent, false);
        TextView name = newRow.findViewById(R.id.userRowUserID);
        name.setText(inName);
        TextView admin = newRow.findViewById(R.id.userRowAdmin);
        if(inAdmin)
            admin.setText(getString(R.string.general_admin));
        else
            admin.setText(getString(R.string.general_user));
        final TextView email = newRow.findViewById(R.id.userRowEmail);
        email.setText(inEmail);
        Button removeUser = newRow.findViewById(R.id.userRowRemoveButton);
        removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser(inEmail);
            }
        });
        if(!currentUser.isAdmin())
            removeUser.setVisibility(View.INVISIBLE);

        TextView N = newRow.findViewById(R.id.userRowNCoordinates);
        TextView W = newRow.findViewById(R.id.userRowWCoordinates);
        if(inN != null || inW != null) {
            N.setText("N: " + inN);
            W.setText("W: " + inW);
        } else {
            N.setText("");
            W.setText(getString(R.string.actor_list_activity_no_location));
        }
        newRow.setId(layoutID);
        parent.addView(newRow);
    }

    private void deleteUser(final String email){

        try{
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", email);
            StringEntity entity = new StringEntity(jsonParams.toString());
            client.delete(this,
                    getString(R.string.system_url) + getString(R.string.system_url_deleteUser),
                    entity,
                    "application/json",
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            System.out.println("Delete user " + email + " was successful");
                            if(email.equals(currentUser.getCurrUserEmail())){
                                currentUser.logout();
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                Toast.makeText(getApplication(),
                                        "Logging out " + currentUser.getCurrUserUserName()
                                        + " as user was deleted from system.",
                                        Toast.LENGTH_LONG).show();
                                startActivity(intent.setClass(getApplication(), MainActivity.class));
                            }
                            refresh();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            System.err.println("Delete user " + email + " failed");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setSensorInfo(String id) {
        Intent intent = new Intent(this, SetSensorLocationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBinder("id", new ObjectWrapperForBinder(id));
        intent.putExtras(bundle);
        startActivity(intent);
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
        getMenuInflater().inflate(R.menu.options_menu_actor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.optionsMenuActorRefresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
        refresh();
    }
}
