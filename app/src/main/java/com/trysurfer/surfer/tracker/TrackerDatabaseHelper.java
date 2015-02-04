package com.trysurfer.surfer.tracker;

/**
 * Created by PRO on 10/9/2014.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrackerDatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_TRACKERS = "trackers";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMMERCIAL_ID = "commercial_id";
    public static final String COLUMN_OPENED = "opened";
    public static final String COLUMN_CLOSED = "closed";
    public static final String COLUMN_SHOWN = "shown";

    private static final String DATABASE_NAME = "trackers.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TRACKERS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_COMMERCIAL_ID
            + " integer not null, " + COLUMN_OPENED + " text not null, "
            + COLUMN_CLOSED + " text not null, " + COLUMN_SHOWN
            + " text not null);";

    public TrackerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    // Fix upgrade without losing data?
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TrackerDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKERS);
        onCreate(db);
    }
}
