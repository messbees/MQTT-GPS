package com.messbees.mqttgps;

public class Message {
    private String unitID;
    private String lat;
    private String lon;
    private String accuracy;

    public Message (String unitID, String lat, String lon, String accuracy) {
        this.unitID = unitID;
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
    }
}
