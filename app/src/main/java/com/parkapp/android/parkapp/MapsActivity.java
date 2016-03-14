package com.parkapp.android.parkapp;

import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AsyncResponse {
    SensorsAsyncTask sensorsAsyncTask = new SensorsAsyncTask();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this to set delegate/listener back to this class
        sensorsAsyncTask.delegate = this;

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //execute the async task
        sensorsAsyncTask.execute();
    }

    //this override the implemented method from AsyncResponse
    public void processFinish(String output) {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.

        try {
            JSONArray sensorsArray = new JSONArray(output);

            LatLng coordinates = new LatLng(-34, 151);
            for (int i = 0, size = sensorsArray.length(); i < size; i++)
            {
                JSONObject sensor = sensorsArray.getJSONObject(i);
                Double lat = Double.parseDouble(sensor.getString("lat"));
                Double lng = Double.parseDouble(sensor.getString("lon"));
                Boolean status = "1".equals(sensor.getString("status"));
                coordinates = new LatLng(lat, lng);
                if (status) {
                    mMap.addMarker(new MarkerOptions().position(coordinates).icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                } else {
                    mMap.addMarker(new MarkerOptions().position(coordinates).icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }

            CameraPosition camPos = new CameraPosition.Builder()
                    .target(coordinates)   //Centramos el mapa en Madrid
                    .zoom(15)         //Establecemos el zoom en 15
                    .build();

            CameraUpdate camUpd =
                    CameraUpdateFactory.newCameraPosition(camPos);
            mMap.animateCamera(camUpd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class SensorsAsyncTask extends AsyncTask<String, String, String> {
        public AsyncResponse delegate = null;

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://10.0.2.2/sensors.php");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            //Do something with the JSON string
            delegate.processFinish(result);
        }
    }


}
