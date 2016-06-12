package com.parkapp.android.parkapp;

/**
 * Created by levi on 12/6/16.
 */
public class MyMarkerObj {
    private Long   id;
    private String lat;
    private String lng;



    public MyMarkerObj() {
        // TODO Auto-generated constructor stub
    }


    public MyMarkerObj(Long id, String lat, String lng)
    {
        this.setId(id);
        this.setLat(lat);
        this.setLng(lng);
    }


    public MyMarkerObj(String lat, String lng)
    {
        this.setLat(lat);
        this.setLng(lng);
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getLat() {
        return lat;
    }


    public void setLat(String lat) {
        this.lat = lat;
    }


    public String getLng() {
        return lng;
    }


    public void setLng(String lng) {
        this.lng = lng;
    }

}
