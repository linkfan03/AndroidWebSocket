package com.thinksouce.vw_websocket;

/**
 * Created by Josh on 11/16/2014.
 */
public enum SignalType {
    RoadWorker("Road Worker", 0),
    BicycleRider("Bicycle Rider", 1),
    Student("Student", 2);

    private String stringValue;
    private int intValue;
    private SignalType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public int toInt(){
        return intValue;
    }
}
