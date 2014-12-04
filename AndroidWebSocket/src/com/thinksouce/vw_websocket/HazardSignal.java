package com.thinksouce.vw_websocket;

import java.util.UUID;

/**
 * Created by Josh on 12/4/2014.
 */
public class HazardSignal {
    public double latitude;
    public double longitude;
    public float bearing;
    public float current_speed;
    public SignalType signalType;
    public UUID deviceID;
    public long dateTime;
}
