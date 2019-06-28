package com.intelligentsensors.firefighting;

import android.content.Context;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelligentsensors.firefighting.Sensors.Sensor;
import com.intelligentsensors.firefighting.util.ObjectWrapperForBinder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LocationDetails extends AppCompatActivity {

    LinearLayout layout;
    TextView locationLabel;
    Sensor sensor;
    AsyncHttpClient client;

    private TextView regSensor;
    private TextView dateModified;
    private TextView temperature;
    private TextView humidity;
    private TextView location;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

        client = new AsyncHttpClient();

        sensor = (Sensor) ((ObjectWrapperForBinder)getIntent().getExtras().getBinder("sensor")).getData();

        layout = findViewById(R.id.detailLayout);
        locationLabel = findViewById(R.id.locationNameLabel);
        locationLabel.setText(sensor.getSensorID());

        regSensor = findViewById(R.id.regTime);
        dateModified = findViewById(R.id.dateMod);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        location = findViewById(R.id.location);
        list = findViewById(R.id.historyRowList);

        possibleFireLayout();
    }

    private void possibleFireLayout(){
        regSensor.setText("Sensor registered on: " + sensor.getSensorRegisterTime());

        if(sensor.getDateModified() != null)
            dateModified.setText("Date modified: " + sensor.getDateModified());
        else ((ViewManager) dateModified.getParent()).removeView(dateModified);

        if(sensor.getTemperature() != null)
            temperature.setText("Current temperature of the area: " + sensor.getTemperature());
        else ((ViewManager) temperature.getParent()).removeView(temperature);

        if(sensor.getHumidity() != null)
            humidity.setText("Humidity of the soil: " + sensor.getHumidity());
        else ((ViewManager) humidity.getParent()).removeView(humidity);

        if(sensor.getPosition() != null){
            location.setText(sensor.getPosition().toString());
        } else ((ViewManager) location.getParent()).removeView(location);

        client.get(this,
                getString(R.string.system_url) +
                getString(R.string.system_url_getSensorData),
                new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                        JSONObject json;
                        JSONObject propertyMap;
                        String sensorID;
                        String date_added;
                        try{
                            for (int i = 0; i < responseBody.length(); i++){
                                json = (JSONObject) responseBody.get(i);
                                propertyMap = json.getJSONObject("propertyMap");
                                sensorID = propertyMap.getString("sensorID");
                                if(sensorID.equals(sensor.getSensorID())){
                                    date_added = propertyMap.getString("date_added");
                                    TextView textView = new TextView(getApplicationContext());
                                    textView.setText(date_added);
                                    list.addView(textView);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                });
    }
}
