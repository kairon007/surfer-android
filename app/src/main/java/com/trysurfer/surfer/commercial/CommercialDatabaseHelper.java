package com.trysurfer.surfer.commercial;

/**
 * Created by PRO on 10/9/2014.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CommercialDatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_COMMERCIALS = "commercials";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMMERCIAL_ID = "commercial_id";
    public static final String COLUMN_PICTURE = "picture";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_PICTURE_BITMAP = "picture_bitmap";

    private static final String DATABASE_NAME = "commercials.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMERCIALS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_COMMERCIAL_ID + " integer not null unique, "
            + COLUMN_PICTURE + " text not null, "
            + COLUMN_URL + " text not null, "
            + COLUMN_PICTURE_BITMAP + " blob);";

    public CommercialDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    // Fix upgrade without losing data?
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(CommercialDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMERCIALS);
        onCreate(db);
    }
}
