package com.trysurfer.surfer.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.LocationBootReceiver;
import com.trysurfer.surfer.LocationService;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.screenlock.ScreenLockService;

import java.util.Calendar;

/**
 * Created by pro on 20.11.2014.
 */
public class ScreenLockReceiverTwo extends BroadcastReceiver {

    private String LOG_TAG = ScreenLockReceiverTwo.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "onReceive");

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(LOG_TAG, "ACTION_SCREEN_OFF");

            SharedPreferences mPrefs = context.getSharedPreferences(
                    Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

            //boolean adCooldown = mPrefs.getBoolean(MainActivity.COMMERCIAL_COOLDOWN, false);
            boolean adCooldown = false;
            if(!adCooldown){
                if(AppUtil.networkAvailable(context)){
                    if (AppUtil.isLoggedIn()) {
                        AppUtil.startScreenLockActivity(context);
                    }
                }
            } else {
                Log.i(LOG_TAG, "Cooldown on");
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(LOG_TAG, "ACTION_SCREEN_ON");
            sendCommercialTracker(context);
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.i(LOG_TAG, "ACTION_USER_PRESENT");
            //openCommercialUrlWithKeyGuard(context);
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(LOG_TAG, "BOOT_COMPLETED");

            //ScreenLockReceiverTwo alarm = new ScreenLockReceiverTwo();
            //alarm.setAlarm(context);

            //Intent screenLockService = new Intent(context, ScreenLockService.class);
            //context.startService(screenLockService);
            AppUtil.startService(context);
        }
    }

    public void setAlarm(Context context) {
        Log.i("LocationReceiver", "setalarm");

        //Intent intent = new Intent(context, ScreenLockReceiverTwo.class);
        //alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        ComponentName receiver = new ComponentName(context, ScreenLockReceiverTwo.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(set_alarm)


    public void cancelAlarm(Context context) {
        Log.i("LocationReceiver", "cancelalarm");
        //if (alarmMgr!= null) {
         //   alarmMgr.cancel(alarmIntent);
        //}

        ComponentName receiver = new ComponentName(context, ScreenLockReceiverTwo.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    // Send commercial tracker for viewing commercial
    private void sendCommercialTracker(Context context) {

        //if (AppUtil.isScreenLockActivityRunning(context)) {
        SharedPreferences mPreferences = context.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

        if(mPreferences.getBoolean("is_ad_loaded", false)){
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("is_ad_loaded", false);
            editor.commit();

            TrackerDAO tracker = new TrackerDAO(0, false,
                    false, true);

            AppUtil.sendCommercialTracker(context, tracker);
        }
    }

    /*
    // Open commercial URL if user chose to open, with keyguard
    private void openCommercialUrlWithKeyGuard(Context context) {

        SharedPreferences mPreferences = context.getSharedPreferences(MainActivity.APP_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();


        boolean openCommercial = mPreferences.getBoolean("open_commercial", false);

        //String commercialUrl = mPreferences.getString("open_commercial_url", "");


        if (openCommercial) {

        }

        //editor.putString("open_commercial_url", "");
        editor.putBoolean("open_commercial", false);
        editor.commit();
    }
    */
}
