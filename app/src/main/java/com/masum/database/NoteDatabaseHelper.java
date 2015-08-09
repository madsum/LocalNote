package com.masum.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by masum on 02/08/15.
 */
public class NoteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "noteTable.db";
    private static final int DATABASE_VERSION = 7;


    public NoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        NoteTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        NoteTable.onUpgrade(database, oldVersion, newVersion);
    }
}
