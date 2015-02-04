package com.trysurfer.surfer.user;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by PRO on 10/27/2014.
 */

public class UserDatabaseHelper extends SQLiteOpenHelper{

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FB_ID = "fb_id";
    public static final String COLUMN_AUTH_TOKEN = "auth_token";
    public static final String COLUMN_EMAIL = "mail";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_BIRTHDAY = "birthday";

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_USERS + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_FB_ID + " integer not null unique, "
            + COLUMN_AUTH_TOKEN + " text not null, "
            + COLUMN_EMAIL + " text not null, "
            + COLUMN_BIRTHDAY + " text, "
            + COLUMN_GENDER + " text" +
            ");";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    // Fix upgrade without losing data?
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(UserDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}

