package com.parkapp.android.parkapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by levi on 12/6/16.
 */
public class LocationsDB extends SQLiteOpenHelper {

    /** Database name */
    private static final String DBNAME            = "favouriteslocations.db";

    /** Version number of the database */
    private static final int    VERSION           = 1;

    /** Field 1 of the table locations, which is the primary key */
    public static final String  FIELD_ROW_ID      = "_id";

    /** Field 2 of the table locations, stores the latitude */
    public static final String  FIELD_LAT         = "lat";

    /** Field 3 of the table locations, stores the longitude */
    public static final String  FIELD_LNG         = "lng";

    /** A constant, stores the the table name */
    public static final String  DATABASE_TABLE    = "locations";
    public static final String  DB_CREATE         = "CREATE TABLE " + DATABASE_TABLE + " ( " +
            FIELD_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
            FIELD_LNG + " TEXT , " +
            FIELD_LAT + " TEXT);";

    /** Constructor */
    public LocationsDB(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null) {
            db.execSQL(DATABASE_TABLE);
            onCreate(db);
        }
    }
}
