package com.parkapp.android.parkapp;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AsyncResponse {
    private GoogleMap mMap;
    private Map<String,Marker> sensors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(36.535736, -6.300415))
                .zoom(16)
                .build();
        CameraUpdate camUpd = CameraUpdateFactory.newCameraPosition(camPos);
        mMap.animateCamera(camUpd);

        callAsynchronousTask();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            executeSensorsAsyncTask("http://10.0.2.2/sensors.php");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        executeSensorsAsyncTask("http://10.0.2.2/sensors.php"); //first execution
        timer.schedule(doAsynchronousTask, 15000, 15000); //execute in every 15000 ms
    }

    public void executeSensorsAsyncTask(String path) {
        SensorsAsyncTask sensorsAsyncTask = new SensorsAsyncTask();
        //this to set delegate/listener back to this class
        sensorsAsyncTask.delegate = this;
        sensorsAsyncTask.execute(path);
    }

    //this override the implemented method from AsyncResponse
    public void processFinish(String output) {
        //Here you will receive the result fired from sensorsAsyncTask class
        //of onPostExecute(result) method.

        try {
            JSONArray sensorsArray = new JSONArray(output);

            for (int i = 0, size = sensorsArray.length(); i < size; i++) {
                addSensorToMap(sensorsArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addSensorToMap(JSONObject sensor) {
        try {
            String sensorId = sensor.getString("sensorID");
            Double lat = Double.parseDouble(sensor.getString("lat"));
            Double lng = Double.parseDouble(sensor.getString("lon"));
            Boolean free = "1".equals(sensor.getString("status"));

            if(sensors.containsKey(sensorId)){
                Marker marker = sensors.get(sensorId);
                marker.remove();
                sensors.remove(sensorId);
            }
            MarkerOptions markerOptions = new MarkerOptions();

            LatLng coordinates = new LatLng(lat, lng);
            if (free) {
                markerOptions.position(coordinates).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            } else {
                markerOptions.position(coordinates).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            sensors.put(sensorId, mMap.addMarker(markerOptions));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
