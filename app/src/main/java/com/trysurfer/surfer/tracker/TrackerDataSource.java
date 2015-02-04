package com.trysurfer.surfer.tracker;

/**
 * Created by PRO on 10/9/2014.
 */
import java.util.ArrayList;
import java.util.List;

import com.trysurfer.surfer.model.TrackerDAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TrackerDataSource {
    private SQLiteDatabase db;
    private TrackerDatabaseHelper dbHelper;
    private String[] allColumns = { TrackerDatabaseHelper.COLUMN_ID,
            TrackerDatabaseHelper.COLUMN_COMMERCIAL_ID,
            TrackerDatabaseHelper.COLUMN_OPENED,
            TrackerDatabaseHelper.COLUMN_CLOSED,
            TrackerDatabaseHelper.COLUMN_SHOWN };

    private static String LOG_TAG = TrackerDataSource.class.getName();

    public TrackerDataSource(Context context) {
        dbHelper = new TrackerDatabaseHelper(context);
    }

    public void open(boolean readOnly) throws SQLException {
        if (db == null || !db.isOpen()) {
            if (readOnly) {
                db =dbHelper.getReadableDatabase();
                Log.v(LOG_TAG, "open readable");
            } else {
                db = dbHelper.getWritableDatabase();
                Log.v(LOG_TAG, "open writeable");
            }
        }
    }

    public void close() {
        if (db.isOpen()) {
            db.close();
            Log.v(LOG_TAG, "close");
        }
    }

    public TrackerDAO createTracker(TrackerDAO tracker) {
        ContentValues values = new ContentValues();
        values.put(TrackerDatabaseHelper.COLUMN_COMMERCIAL_ID,
                tracker.getCommercialId());
        values.put(TrackerDatabaseHelper.COLUMN_OPENED, tracker.isOpened());
        values.put(TrackerDatabaseHelper.COLUMN_CLOSED, tracker.isClosed());
        values.put(TrackerDatabaseHelper.COLUMN_SHOWN, tracker.isShown());

        long insertId = db.insert(TrackerDatabaseHelper.TABLE_TRACKERS, null,
                values);
        Cursor cursor = db.query(TrackerDatabaseHelper.TABLE_TRACKERS,
                allColumns, TrackerDatabaseHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null);
        cursor.moveToFirst();
        TrackerDAO newTracker = cursorToTracker(cursor);
        cursor.close();

        Log.i(LOG_TAG, "stored tracker with id: " + newTracker.getId());

        return newTracker;
    }

    public void deleteCommercial(TrackerDAO tracker) {
        long id = tracker.getId();
        Log.i(LOG_TAG, "Tracker deleted with id: " + id);
        db.delete(TrackerDatabaseHelper.TABLE_TRACKERS,
                TrackerDatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public List<TrackerDAO> getAllTrackers() {
        Log.v(LOG_TAG, "getAllTrackers");
        List<TrackerDAO> trackers = new ArrayList<TrackerDAO>();

        Cursor cursor = db.query(TrackerDatabaseHelper.TABLE_TRACKERS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TrackerDAO tracker = cursorToTracker(cursor);
            trackers.add(tracker);
            cursor.moveToNext();
        }

        cursor.close();
        return trackers;
    }

    private TrackerDAO cursorToTracker(Cursor cursor) {
        TrackerDAO tracker = new TrackerDAO();
        tracker.setId(cursor.getLong(0));
        tracker.setCommercialId(cursor.getLong(1));
        tracker.setOpened(cursor.getInt(2) > 0);
        tracker.setClosed(cursor.getInt(3) > 0);
        tracker.setShown(cursor.getInt(4) > 0);

        return tracker;
    }
}

