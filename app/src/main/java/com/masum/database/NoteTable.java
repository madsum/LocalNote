package com.masum.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.masum.locationnote.MainActivity;

/**
 * Created by masum on 02/08/15.
 */
public class NoteTable {

    // Database table
    public static final String TABLE_NOTE = "note";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitue";
    public static final String COLUMN_IMAGE = "image";


    // Database creation SQL statement
    private static final String DATABASE_CREATE = "CREATE TABLE " +
            TABLE_NOTE
            + "( "
            + COLUMN_ID + " INTEGER PRIMARY  KEY AUTOINCREMENT, "
            + COLUMN_TITLE  +" VARCHAR(255), "
            + COLUMN_DESCRIPTION + " VARCHAR(1024), "
            + COLUMN_DATE + " VARCHAR(255),"
            + COLUMN_LATITUDE + " VARCHAR(30),"
            + COLUMN_LONGITUDE + " VARCHAR(30),"
            + COLUMN_IMAGE + " VARCHAR(255)"
            +");";
    // Database drop table SQL statement
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NOTE;


    public static void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(DATABASE_CREATE);
            Log.i(MainActivity.TAG, "table created successfully");
        }catch (Exception e) {
            //System.out.println("ex: "+e.getMessage());
            Log.i(MainActivity.TAG, "exception: "+e.getMessage());
        }

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(MainActivity.TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL(DROP_TABLE);
        onCreate(database);
    }

}
