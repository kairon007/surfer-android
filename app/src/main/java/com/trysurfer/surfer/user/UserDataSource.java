package com.trysurfer.surfer.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trysurfer.surfer.model.UserDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PRO on 10/27/2014.
 */
public class UserDataSource {

    private SQLiteDatabase db;
    private UserDatabaseHelper dbHelper;
    private String[] allColumns = {
            UserDatabaseHelper.COLUMN_ID,
            UserDatabaseHelper.COLUMN_FB_ID,
            UserDatabaseHelper.COLUMN_AUTH_TOKEN,
            UserDatabaseHelper.COLUMN_EMAIL,
            UserDatabaseHelper.COLUMN_GENDER,
            UserDatabaseHelper.COLUMN_BIRTHDAY
    };

    private static String LOG_TAG = UserDataSource.class.getName();

    public UserDataSource(Context context) {
        dbHelper = new UserDatabaseHelper(context);
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

    public UserDAO createUser(UserDAO user) {
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_ID, user.getId());
        values.put(UserDatabaseHelper.COLUMN_FB_ID, user.getFbId());
        values.put(UserDatabaseHelper.COLUMN_AUTH_TOKEN, user.getAuth_token());
        values.put(UserDatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(UserDatabaseHelper.COLUMN_BIRTHDAY, user.getBirthday());
        values.put(UserDatabaseHelper.COLUMN_GENDER, user.getGender());

        long insertId = db.insert(UserDatabaseHelper.TABLE_USERS,
                null, values);
        Cursor cursor = db.query(UserDatabaseHelper.TABLE_USERS,
                allColumns, UserDatabaseHelper.COLUMN_ID + " = "
                        + insertId, null, null, null, null);
        cursor.moveToFirst();
        UserDAO newUser = cursorToUser(cursor);
        cursor.close();

        Log.i(LOG_TAG, "User ID:" + newUser.getId() + " stored");

        return newUser;
    }

    public void deleteCommercial(UserDAO commercial) {
        long id = commercial.getId();
        Log.i(LOG_TAG, "User deleted with id: " + id);
        db.delete(UserDatabaseHelper.TABLE_USERS,
                UserDatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteAllUsers(){
        Cursor cursor = db.query(UserDatabaseHelper.TABLE_USERS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserDAO user = cursorToUser(cursor);
            Log.i(LOG_TAG, "User deleted with id: " + user.getId());
            db.delete(UserDatabaseHelper.TABLE_USERS,
                    UserDatabaseHelper.COLUMN_ID + " = " + user.getId(), null);
            cursor.moveToNext();
        }

        cursor.close();
    }

    public List<UserDAO> getAllUsers() {
        //Log.i(LOG_TAG, "getAllcommercials");
        List<UserDAO> users = new ArrayList<UserDAO>();

        Cursor cursor = db.query(UserDatabaseHelper.TABLE_USERS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserDAO user = cursorToUser(cursor);
            users.add(user);
            cursor.moveToNext();
        }

        cursor.close();

        return users;
    }

    public UserDAO getUser(String fb_id) {
        //Log.i(LOG_TAG, "getAllcommercials");
        UserDAO user = new UserDAO();

        Cursor cursor =  this.db.rawQuery("select * from " + UserDatabaseHelper.TABLE_USERS +
                " where " + UserDatabaseHelper.COLUMN_FB_ID + "='" + fb_id + "'" , null);

        if(cursor != null && cursor.moveToFirst()){
            UserDAO userQuery = cursorToUser(cursor);
            user = userQuery;
        }

        cursor.close();

        return user;
    }

    private UserDAO cursorToUser(Cursor cursor) {
        UserDAO user = new UserDAO();
        user.setId(cursor.getLong(0));
        user.setFbId(cursor.getString(1));
        user.setAuth_token(cursor.getString(2));
        user.setEmail(cursor.getString(3));
        user.setBirthday(cursor.getString(4));
        user.setGender(cursor.getString(5));

        return user;
    }

    // Needs fix
    public void updateUser(UserDAO user) {
        /*
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_PICTURE,
                commercial.getPicture());
        values.put(UserDatabaseHelper.COLUMN_URL, commercial.getUrl());
        values.put(UserDatabaseHelper.COLUMN_PICTURE_BITMAP,
                commercial.getPictureBitmap());

        db.update("commercial", values, "id = " + commercial.getId(), null);
        */
    }
}

