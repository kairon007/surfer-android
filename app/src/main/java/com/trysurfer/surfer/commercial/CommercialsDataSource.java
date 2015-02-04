package com.trysurfer.surfer.commercial;

/**
 * Created by PRO on 10/9/2014.
 */
import java.util.ArrayList;
import java.util.List;

import com.trysurfer.surfer.model.CommercialDAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CommercialsDataSource {
    private SQLiteDatabase db;
    private CommercialDatabaseHelper dbHelper;
    private String[] allColumns = { CommercialDatabaseHelper.COLUMN_ID,
            CommercialDatabaseHelper.COLUMN_COMMERCIAL_ID,
            CommercialDatabaseHelper.COLUMN_PICTURE,
            CommercialDatabaseHelper.COLUMN_URL,
            CommercialDatabaseHelper.COLUMN_PICTURE_BITMAP };

    private static String LOG_TAG = CommercialsDataSource.class.getName();

    public CommercialsDataSource(Context context) {
        dbHelper = new CommercialDatabaseHelper(context);
    }

    public void open(boolean readOnly) throws SQLException {
        if (db == null || !db.isOpen()) {
            if (readOnly) {
                db = dbHelper.getReadableDatabase();
                //Log.v(LOG_TAG, "open readable");
            } else {
                db = dbHelper.getWritableDatabase();
                //Log.v(LOG_TAG, "open writeable");
            }
        }
    }

    public void close() {
        //Log.v(LOG_TAG, "close");
        if (db.isOpen()) {
            db.close();
        }
    }

    public CommercialDAO createCommercial(CommercialDAO commercial) {
        ContentValues values = new ContentValues();
        values.put(CommercialDatabaseHelper.COLUMN_COMMERCIAL_ID,
                commercial.getId());
        values.put(CommercialDatabaseHelper.COLUMN_PICTURE,
                commercial.getPicture());
        values.put(CommercialDatabaseHelper.COLUMN_PICTURE_BITMAP,
                commercial.getPictureBitmap());
        values.put(CommercialDatabaseHelper.COLUMN_URL, commercial.getUrl());

        long insertId = db.insert(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                null, values);
        Cursor cursor = db.query(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                allColumns, CommercialDatabaseHelper.COLUMN_ID + " = "
                        + insertId, null, null, null, null);
        cursor.moveToFirst();
        CommercialDAO newCommercial = cursorToCommercial(cursor);
        cursor.close();

        Log.i(LOG_TAG, "Commercial ID:" + newCommercial.getId() + " stored");

        return newCommercial;
    }

    public void deleteCommercial(CommercialDAO commercial) {
        long id = commercial.getId();
        Log.i(LOG_TAG, "Comment deleted with id: " + id);
        db.delete(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                CommercialDatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteAllCommercials(){
        Cursor cursor = db.query(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CommercialDAO commercial = cursorToCommercial(cursor);
            Log.i(LOG_TAG, "Comment deleted with id: " + commercial.getId());
            db.delete(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                    CommercialDatabaseHelper.COLUMN_ID + " = " + commercial.getId(), null);
            cursor.moveToNext();
        }

        cursor.close();
    }

    public List<CommercialDAO> getAllCommercials() {
        //Log.i(LOG_TAG, "getAllcommercials");
        List<CommercialDAO> commercials = new ArrayList<CommercialDAO>();

        Cursor cursor = db.query(CommercialDatabaseHelper.TABLE_COMMERCIALS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CommercialDAO commercial = cursorToCommercial(cursor);
            commercials.add(commercial);
            cursor.moveToNext();
        }

        cursor.close();

        return commercials;
    }

    private CommercialDAO cursorToCommercial(Cursor cursor) {
        CommercialDAO commercial = new CommercialDAO();
        commercial.setId(cursor.getLong(0));
        commercial.setCommercialId(cursor.getLong(1));
        commercial.setPicture(cursor.getString(2));
        commercial.setUrl(cursor.getString(3));
        commercial.setPictureBitmap(cursor.getBlob(4));

        return commercial;
    }

    // Needs fix
    public void updateCommercial(CommercialDAO commercial) {
        ContentValues values = new ContentValues();
        values.put(CommercialDatabaseHelper.COLUMN_PICTURE,
                commercial.getPicture());
        values.put(CommercialDatabaseHelper.COLUMN_URL, commercial.getUrl());
        values.put(CommercialDatabaseHelper.COLUMN_PICTURE_BITMAP,
                commercial.getPictureBitmap());

        db.update("commercial", values, "id = " + commercial.getId(), null);
    }
}
