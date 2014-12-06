package com.thinksouce.vw_websocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by Josh on 12/4/2014.
 */
public class HazardSignal {
    public HazardSignal(JSONObject jsonObject) throws JSONException{
        latitude = jsonObject.getDouble("latitude");
        longitude = jsonObject.getDouble("longitude");
        bearing = Float.parseFloat(jsonObject.getString("bearing"));
        current_speed = Float.parseFloat(jsonObject.getString("current_speed"));
        signalType = SignalType.values()[jsonObject.getInt("signalType")];
        deviceID = UUID.fromString(jsonObject.getString("deviceID"));
        dateTime = Long.parseLong(jsonObject.getString("dateTime"));
    }
    public double latitude;
    public double longitude;
    public float bearing;
    public float current_speed;
    public SignalType signalType;
    public UUID deviceID;
    public long dateTime;

    public void warnLocation(){

    }
    public void warnBearing(){

    }
    public void warnSpeed(){

    }
}