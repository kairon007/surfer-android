package com.trysurfer.surfer;

/**
 * Created by PRO on 10/9/2014.
 */

import com.facebook.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trysurfer.surfer.commercial.CommercialAsyncTask;
import com.trysurfer.surfer.commercial.CommercialImageDownloader;
import com.trysurfer.surfer.commercial.CommercialsDataSource;
import com.trysurfer.surfer.model.CommercialDAO;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.screenlock.AdMobLockScreen;
import com.trysurfer.surfer.screenlock.ScreenLockService;
import com.trysurfer.surfer.screenlock.StartScreenLockAsyncTask;
import com.trysurfer.surfer.tracker.CommercialTrackerAsyncTask;
import com.trysurfer.surfer.user.UserAsyncTask;
import com.trysurfer.surfer.tracker.TrackerDataSource;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.List;

public class AppUtil {

    private static String LOG_TAG = AppUtil.class.getName();

    // Check if internet is available
    public static boolean networkAvailable(Context context) {
        // Log.v(LOG_TAG, "networkAvailable");
        boolean wifiAvailable = false;
        boolean mobileAvailable = false;
        ConnectivityManager conManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    wifiAvailable = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    mobileAvailable = true;
        }
        return wifiAvailable || mobileAvailable;
    }

    // Test method. Check connection w/ toast-response
    public static boolean networkAvailableWithToast(Context context) {
        if (!networkAvailable(context)) {
            Toast.makeText(context, "No active internet connection ...",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public static boolean isStringValid(String string) {
        // Log.v(LOG_TAG, "isStringValid");
        if (string != null) {
            if (!(string.equals(""))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUrlValid(String url) {
        // Log.v(LOG_TAG, "isUrlValid");
        if (isStringValid(url)) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                // url = "http://" + url;
                return true;
            }
        }
        return false;
    }

    // Check if ScreenLockActivity is running. Should not use?
    public static boolean isScreenLockActivityRunning(Context context) {
        // Log.v(LOG_TAG, "isScreenLockActivityRunning");
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        /*
        List<RunningTaskInfo> activitys = activityManager
                .getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < activitys.size(); i++) {
            if (activitys.get(i).topActivity
                    .toString()
                    .equalsIgnoreCase(
                            "ComponentInfo{com.trysurfer.surfer/com.trysurfer.surfer.activity_screenlock.AdMobLockScreenActivity}")) {
                return true;
            }
        }
        */

        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (AdMobLockScreen.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Check if logged in. Better way?
    public static boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (session.isOpened()) {
                return true;
            } else if (session.isClosed()) {
                return false;
            }
        }
        return false;
    }

	/*
	 * private void launchScreenlockService(Context context) { if
	 * (isUserAndCommercialsStored()) { AppUtil.startService(context); } }
	 */

    public static void startUserAsyncTask(Context context, String fb_id,
                                          String email, String birthday, String gender) {
        try {
            if (fb_id != null && email != null) {
                try {
                    new UserAsyncTask(context).execute(fb_id, email, birthday, gender);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error: " + e);
                    Toast msg = Toast.makeText(context, "user failed",
                            Toast.LENGTH_LONG);
                    msg.show();
                }
            }
        } catch (NumberFormatException nfe) {

        } catch (Exception e) {

        }
    }

    // Start ScreenlockService
    /*
    public static void startService(Context context, String fb_id, String email) {
        Log.v(LOG_TAG, "startService");
        //if (isUserAndCommercialsStored(context, fb_id, email)) {
            if (!isScreenLockServiceRunning(context)) {
                context.startService(new Intent(context,
                        ScreenLockService.class));
            }
        //}
    }
    */

    public static void startService(Context context) {
        Log.v(LOG_TAG, "startService");
        if (!isScreenLockServiceRunning(context)) {
            Intent startServiceIntent = new Intent(context, ScreenLockService.class);
            startServiceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            context.startService(startServiceIntent);
        }
    }

    // Stop ScreenlockService
    public static void stopService(Context context) {
        Log.v(LOG_TAG, "stopService");
        if (isScreenLockServiceRunning(context)) {
            //Intent serviceIntent = new Intent(context, ScreenLockService.class);
            //serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            //context.startService(new Intent(context, ScreenLockService.class));

            context.stopService(new Intent(context, ScreenLockService.class));
        }
    }

    // SJEKK OM BRUKER ER LAGRET
    private static boolean isUserInfoStored(Context context) {
        SharedPreferences mPreferences = context.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

        String fb_id = mPreferences.getString(Constants.SHAREDPREFS.USER_FB_ID, "");
        String email = mPreferences.getString(Constants.SHAREDPREFS.USER_EMAIL, "");

        if (isStringValid(fb_id) && isStringValid(email)) {
            return true;
        }
        return false;
    }

    // SJEKK OM COMMERCIALS ER LAGRET
	/*
	 * private boolean isCommercialsStored() { return true; }
	 */

    /*
    private static boolean isUserAndCommercialsStored(Context context,
                                                      String fb_id, String email) {
        if (isUserInfoStored(context)) {
            return true;
        } else {
            startUserAsyncTask(context, fb_id, email);
        }
        return false;
    }
    */

    // Check if ScreenLockService is running. Should not use?
    public static boolean isScreenLockServiceRunning(Context context) {
        // Log.v(LOG_TAG, "isScreenLockServiceRunning");
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (ScreenLockService.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

	/*
	 * public static void userLogout() { //
	 * editor.putBoolean("commercials_updated", false); }
	 *
	 * public static void userLogin() {
	 *
	 * }
	 */

    // COMMERCIALS

    // Download images and store in DB
    public static void saveImages(List<CommercialDAO> commercialList,
                                  Context context) {
        Log.v(LOG_TAG, "saveImages");
        if (commercialList != null) {
            deleteAllCommercials(context);
            CommercialImageDownloader.download(commercialList, context);
        }
    }

    // Delete commercials from DB
    public static void deleteAllCommercials(Context context) {
        CommercialsDataSource datasource = new CommercialsDataSource(context);
        try {
            datasource.open(false);
            datasource.deleteAllCommercials();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            datasource.close();
        }
    }

    public static void fetchCommercials(Context activity) {
        if (AppUtil.networkAvailableWithToast(activity)) {
            try {
                // String uri = getString(R.string.commercial_uri);
                new CommercialAsyncTask(activity).execute();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error: " + e);
                Toast msg = Toast.makeText(activity, "Commercial failed",
                        Toast.LENGTH_SHORT);
                msg.show();
            }
        }
    }

    // SCREENLOCK


    // Start ScreenLockActivity
    public static void startScreenLockActivity(Context context) {
        Log.v(LOG_TAG, "startScreenLockActivity");

		/*
		 * Intent screenLock = new Intent(context.getApplicationContext(),
		 * ScreenLockActivity.class);
		 * screenLock.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		 * Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
		 * Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 * context.getApplicationContext().startActivity(screenLock);
		 */

        try {
            new StartScreenLockAsyncTask(context).execute();
        } catch (Exception e) {

        }
    }

    // Open commercial url in browser
    public static void openCommercialUrl(Context context, String commercialUrl) {
        Log.v(LOG_TAG, "openCommercialUrl");
        if (AppUtil.networkAvailable(context)) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(commercialUrl));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            context.startActivity(i);
        }
    }

    /**
     * Tracker
     */

    // Send commercial tracker
    public static void sendCommercialTracker(Context context, TrackerDAO tracker) {
        Log.v(LOG_TAG, "sendCommercialTracker");
        if (AppUtil.networkAvailable(context)) {
            try {
                new CommercialTrackerAsyncTask(context).execute(tracker);
            } catch (Exception e) {
                Log.e(LOG_TAG, "error: " + e.getLocalizedMessage());
                Toast msg = Toast.makeText(context, "Feil: " + e,
                        Toast.LENGTH_LONG);
                msg.show();
            }
        } else {
            //storeCommercialTracker(context, tracker);
        }
    }

    // Store tracker in DB
    public static void storeCommercialTracker(Context context,
                                              TrackerDAO tracker) {
        Log.v(LOG_TAG, "storeCommercialTracker");

        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPrefs.edit();
        mEditor.putBoolean(Constants.SHAREDPREFS.TRACKERS_STORED, true);
        Gson gson = new Gson();
        Type type = new TypeToken<List<TrackerDAO>>(){}.getType();

        List<TrackerDAO> trackerList = gson.fromJson(Constants.SHAREDPREFS.TRACKERS_LIST, type);
        if(trackerList != null){
            trackerList.add(tracker);

            mEditor.putBoolean(Constants.SHAREDPREFS.TRACKERS_STORED, true);
            mEditor.putString(Constants.SHAREDPREFS.TRACKERS_LIST, gson.toJson(trackerList));
            mEditor.commit();
        }

        /*
        TrackerDataSource datasource = new TrackerDataSource(context);
        datasource.open(false);
        datasource.createTracker(tracker);
        datasource.close();
        */
    }

    // Check if any trackers are stored, if so, send them and delete from DB.
    public static void checkIfTrackersStored(Context context) {
        Log.v(LOG_TAG, "checkIfTrackersStored");
        //TrackerDataSource datasource = new TrackerDataSource(context);

        // FIX WITH SHAREDPREFS

        SharedPreferences mSharedPrefs = context.getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS,
                Context.MODE_PRIVATE);
        if(mSharedPrefs.getBoolean(Constants.SHAREDPREFS.TRACKERS_STORED, false)){
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<TrackerDAO>>(){}.getType();

                List<TrackerDAO> trackerList = gson.fromJson(Constants.SHAREDPREFS.TRACKERS_LIST, type);

                Log.i(LOG_TAG, "trackerlistsize: " + trackerList.size());
                if (trackerList.size() > 0 && trackerList != null) {
                    Log.i(LOG_TAG, "checkIfTrackersStored. listsize: "
                            + trackerList.size());
                    SharedPreferences.Editor mEditor = mSharedPrefs.edit();

                    for (int i = 0; i < trackerList.size(); i++) {
                        sendCommercialTracker(context, trackerList.get(i));
                    }
                    mEditor.putBoolean(Constants.SHAREDPREFS.TRACKERS_STORED, false);
                    mEditor.putString(Constants.SHAREDPREFS.TRACKERS_LIST, "");
                    mEditor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //datasource.close();
            }
        }



        /*
        try {
            datasource.open(false);
            List<TrackerDAO> trackerList = datasource.getAllTrackers();
            Log.i(LOG_TAG, "trackerlistsize: " + trackerList.size());
            if (trackerList.size() > 0 && trackerList != null) {
                Log.i(LOG_TAG, "checkIfTrackersStored. listsize: "
                        + trackerList.size());
                for (int i = 0; i < trackerList.size(); i++) {
                    sendCommercialTracker(context, trackerList.get(i));
                    datasource.deleteCommercial(trackerList.get(i));
                    Log.i(LOG_TAG, "deleted tracker with id: "
                            + trackerList.get(i).getId());
                }
            }
        } catch (Exception e) {

        } finally {
            datasource.close();
        }
        */
    }
}
