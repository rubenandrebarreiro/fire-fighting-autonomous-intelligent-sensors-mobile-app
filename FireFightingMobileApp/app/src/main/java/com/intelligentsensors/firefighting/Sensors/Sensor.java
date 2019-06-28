package com.intelligentsensors.firefighting.Sensors;

import com.google.android.gms.maps.model.LatLng;

public class Sensor {

    private String id;
    private LatLng latlon;
    private String sensorRegisterTime;
    private String dateModified;
    private String temperature;
    private String humidity;

    public Sensor(String id, LatLng latlon, String sensorRegisterTime, String dateModified, String temperature, String humidity) {
        this.id = id;
        this.latlon = latlon;
        this.sensorRegisterTime = sensorRegisterTime;
        this.dateModified = dateModified;
        this.temperature = temperature;
        this.humidity = humidity;
    }
    public Sensor(String id, String sensorRegisterTime, String dateModified, String temperature, String humidity){
        this.id = id;
        latlon = null;
        this.sensorRegisterTime = sensorRegisterTime;
        this.dateModified = dateModified;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public String getSensorID(){
        return id;
    }

    public LatLng getPosition(){
        return latlon;
    }

    public String getSensorRegisterTime(){
        return sensorRegisterTime;
    }

    public String getDateModified(){
        return dateModified;
    }

    public String getTemperature(){
        return temperature;
    }

    public String getHumidity(){
        return humidity;
    }

}
