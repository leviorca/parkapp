package com.parkapp.android.parkapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.LocationListener;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AsyncResponse, LocationListener {
    private GoogleMap mMap;
    private Map<String, Marker> sensors = new HashMap<>();
    private List<Marker> freeParkings = new ArrayList<>();
    private List<Marker> favourites = new ArrayList<>();
    private LocationManager locationManager;

    Context context = this;
    FavouritesDataSource Fdata;
    MyMarkerObj Mobj;
    static final ContentValues contentValues = new ContentValues();
    boolean dbCheckedOpen = false;
    
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
        Fdata = new FavouritesDataSource(context);
        Mobj = new MyMarkerObj();

        try {
            Fdata.open();
            dbCheckedOpen = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Cursor cursor = Fdata.db.query(LocationsDB.DATABASE_TABLE, Fdata.cols, null, null, null, null, null);
        if (cursor != null) {

            List<MyMarkerObj> mmo = Fdata.getMarkers();
            for (int i = 0; i < mmo.size(); i++)
            {
                String latidutes = mmo.get(i).getLat().toString();
                String langidutes = mmo.get(i).getLng().toString();
                LatLng latlng = new LatLng(Double.valueOf(latidutes), Double.valueOf(langidutes));
                addFavouriteMarker(latlng);
            }//for
        }//if

        testDialog();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable location
            mMap.setMyLocationEnabled(true);
            // Getting LocationManager object from System Service LOCATION_SERVICE
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Getting Current Location
            Location location = getCurrentLocation();

            if (location != null) {
                onLocationChanged(location);
            }
            //locationManager.requestLocationUpdates(provider, 20000, 0, (android.location.LocationListener) this);
        } else {
            // Show rationale and request permission.
        }

        callAsynchronousTask();
    }

    public void testDialog()
    {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(final LatLng point) {
                if (dbCheckedOpen == false)
                {
                    try {
                        Fdata.open();
                    }
                    catch (Exception e) {
                    }
                }
                Mobj.setLat(String.valueOf(point.latitude));
                Mobj.setLng(String.valueOf(point.longitude));
                final LatLng latlng = new LatLng(Double.valueOf(Mobj.getLat()), Double.valueOf(Mobj.getLng()));

                // Setting latitude in ContentValues
                contentValues.put(LocationsDB.FIELD_LAT, Mobj.getLat());

                // Setting longitude in ContentValues
                contentValues.put(LocationsDB.FIELD_LNG, Mobj.getLng());

                addFavouriteMarker(latlng);

                Fdata.addMarker();
                Fdata.close();
                dbCheckedOpen = false;

            }//onMapLongClick

        }); //setOnMapLongClickListener

    }//reminderDialog

    @Override
    public void onLocationChanged(Location location) {
        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        CameraPosition camPos = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16)
                .build();
        CameraUpdate camUpd = CameraUpdateFactory.newCameraPosition(camPos);
        mMap.animateCamera(camUpd);
    }

    public void callAsynchronousTask() {
        final String path = "http://172.16.96.129/sensors.php";
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            executeSensorsAsyncTask(path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        executeSensorsAsyncTask(path); //first execution
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
            int free = Integer.parseInt(sensor.getString("status"));

            Marker marker;
            LatLng coordinates = new LatLng(lat, lng);
            if (sensors.containsKey(sensorId)) {
                marker = sensors.get(sensorId);
                marker.setPosition(coordinates);
            } else {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(coordinates);
                marker = mMap.addMarker(markerOptions);
                sensors.put(sensorId, marker);
            }

            marker.setVisible(true);

            if (free >= 1) {
                if (free > 1) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.parking_ico_g));
                } else {
                    if (!freeParkings.contains(marker)) {
                        freeParkings.add(marker);
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.parking_ico_b));
                }
            } else {
                if (freeParkings.contains(marker)) {
                    freeParkings.remove(marker);
                }
                marker.setVisible(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Location getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Show rationale and request permission.
        }
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        return locationManager.getLastKnownLocation(provider);
    }

    /** Called when the user touches the button */
    public void searchNearbyParking (View view) {
        // Do something in response to button click

        Location currentLocation = getCurrentLocation();
        Marker nearby = searchNearbyMarker(currentLocation);
        if(nearby != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(nearby.getPosition()), 250, null);
            nearby.showInfoWindow();
        }
    }

    public Marker searchNearbyMarker(Location originLocation) {
        // Do something in response to button click
        Marker nearby = null;
        Location targetLocation = new Location("");
        float shorterDistance = -1;
        for (Marker marker : freeParkings) {
            targetLocation.setLatitude(marker.getPosition().latitude);
            targetLocation.setLongitude(marker.getPosition().longitude);

            float distance = originLocation.distanceTo(targetLocation);
            if (shorterDistance == -1 || distance < shorterDistance) {
                shorterDistance = distance;
                nearby = marker;
            }
        }
        String title = getAddressFromMarker(nearby);
        title += " (" + String.format("%.2f", shorterDistance) + " m)";
        nearby.setTitle(title);
        return nearby;
    }

    public String getAddressFromMarker(Marker marker) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "Dirección: ";

        try {
            address += geocoder.getFromLocation( marker.getPosition().latitude, marker.getPosition().longitude, 1 ).get( 0 ).getAddressLine( 0 );
        } catch (IOException e) {
            address += "Not_defined";
        }
        return address;
    }

    public void addFavouriteMarker(LatLng latlng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng));
        favourites.add(marker);
    }
}
