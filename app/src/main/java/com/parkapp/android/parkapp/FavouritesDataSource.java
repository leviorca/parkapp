package com.parkapp.android.parkapp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by levi on 12/6/16.
 */
public class FavouritesDataSource {
    LocationsDB         favouritesDB;
    SQLiteDatabase      db;
    final String[]      cols = { LocationsDB.FIELD_LAT, LocationsDB.FIELD_LNG };
    MapsActivity        main;


    public FavouritesDataSource(Context C) {
        favouritesDB = new LocationsDB(C);
    }


    public void open() throws SQLException
    {
        db = favouritesDB.getWritableDatabase();
    }


    public void close()
    {
        db.close();
    }


    public void addMarker()
    {
        main = new MapsActivity();
        db.insert(LocationsDB.DATABASE_TABLE, null, main.contentValues);
    }


    public List<MyMarkerObj> getMarkers() {
        List<MyMarkerObj> markers = new ArrayList<MyMarkerObj>();
        Cursor cursor = db.query(LocationsDB.DATABASE_TABLE, cols, null, null, null, null, null);
        cursor.moveToFirst();
        while ( !cursor.isAfterLast())
        {
            MyMarkerObj mmo = cursorToMarker(cursor);
            markers.add(mmo);
            cursor.moveToNext();
        }
        cursor.close();

        return markers;
    }


    private MyMarkerObj cursorToMarker(Cursor cursor) {
        MyMarkerObj mmo = new MyMarkerObj();
        mmo.setLat(cursor.getString(0));
        mmo.setLng(cursor.getString(1));
        return mmo;
    }
}
