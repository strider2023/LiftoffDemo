package com.liftoff.demo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arindamnath on 08/12/15.
 */
public class DBUtil {

    private class DBHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "_carpoolDatabase.db";
        private static final int DB_VERSION = 1;

        public final String LOCATIONS = "_locations";
        public final String LOCATIONS_ROW_ID = "_id";
        public final String LOCATIONS_ADDRESS = "_address";
        public final String LOCATIONS_DATETIME = "_createdOn";

        public final String DB_CREATE_TABLE_LOCATIONS = "create table  " + LOCATIONS +
                "(" + LOCATIONS_ROW_ID + " integer primary key autoincrement, " +
                LOCATIONS_ADDRESS + " text not null, " +
                LOCATIONS_DATETIME + " text not null);";

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TABLE_LOCATIONS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_CREATE_TABLE_LOCATIONS);
            onCreate(db);
        }
    }

    private DBHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private static DBUtil adapter;

    private DBUtil(Context context) {
        dbHelper = new DBHelper(context);
    }

    public static DBUtil getInstance(Context context) {
        if (adapter == null)
            adapter = new DBUtil(context);
        return adapter;
    }

    public void openCache() throws Exception {
        mDatabase = dbHelper.getWritableDatabase();
    }

    public void closeCache() {
        dbHelper.close();
    }

    public long insertNewEvent(String address) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        ContentValues courseTableValues = new ContentValues();
        courseTableValues.put(dbHelper.LOCATIONS_ADDRESS, address);
        courseTableValues.put(dbHelper.LOCATIONS_DATETIME, dateFormat.format(new Date()));
        return mDatabase.insert(dbHelper.LOCATIONS, null, courseTableValues);
    }

    public boolean deleteAllLocations() {
        return mDatabase.delete(dbHelper.LOCATIONS, "1", null) > 0;
    }
}

