package com.example.fleps.trackmap.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Fleps_000 on 05.09.2015.
 */
public class TrackPoint {

    private static final DateFormat TIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private double latitude;
    private double longitude;
    private double speed;
    private double azimuth;
    private Date time;

    public TrackPoint(double latitude, double longitude, double speed, double azimuth, String time) throws ParseException {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.azimuth = azimuth;
        this.time = TIME_FORMAT.parse(time);
    }

    public TrackPoint() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
