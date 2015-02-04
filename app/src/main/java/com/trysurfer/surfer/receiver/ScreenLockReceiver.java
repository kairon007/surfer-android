package com.trysurfer.surfer.receiver;

/**
 * Created by PRO on 10/9/2014.
 */
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.LocationBootReceiver;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.screenlock.ScreenLockActivity;
import com.trysurfer.surfer.screenlock.ScreenLockActivityOld;

public class ScreenLockReceiver extends BroadcastReceiver {

    private boolean openCommercial;
    private static ScreenLockActivityOld screenLockActivity = null;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;

    private static final String LOG_TAG = ScreenLockReceiver.class.getName();

    public static void setMainActivityHandler(ScreenLockActivityOld sla) {
        screenLockActivity = sla;
    }

    public ScreenLockReceiver(){
        // NOT KILLABLE?
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "onReceive");

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(LOG_TAG, "ACTION_SCREEN_OFF");

            SharedPreferences mPrefs = context.getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

            boolean adCooldown = mPrefs.getBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false);
            //boolean adCooldown = false;
            if(!adCooldown){
                if(AppUtil.networkAvailable(context)){
                    if (AppUtil.isLoggedIn()) {
                        AppUtil.startScreenLockActivity(context);
                    }
                } else {
                    // Image lockscreen
                }
            } else {
                Log.i(LOG_TAG, "Cooldown on");
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(LOG_TAG, "ACTION_SCREEN_ON");
            sendCommercialTracker(context);
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.i(LOG_TAG, "ACTION_USER_PRESENT");

            /*
            mPreferences = context.getSharedPreferences("current_commercial",
                    Context.MODE_PRIVATE);
            openCommercial = mPreferences.getBoolean("open_commercial", false);

            if (openCommercial) {
                openCommercialUrlWithKeyGuard(context);
            }
            */
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(LOG_TAG, "ACTION_BOOT_COMPLETE");
            AppUtil.startService(context);
        }
    }

    /*
    public void setAlarm(Context context) {
        Log.i("LocationReceiver", "setalarm");
        //alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LocationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        ComponentName receiver = new ComponentName(context, ScreenLockBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(set_alarm)


    public void cancelAlarm(Context context) {
        Log.i("LocationReceiver", "cancelalarm");
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        ComponentName receiver = new ComponentName(context, LocationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    */

    // Send commercial tracker for viewing commercial
    private void sendCommercialTracker(Context context) {
        if (AppUtil.isScreenLockActivityRunning(context)) {
            /*
            mPreferences = context.getSharedPreferences("current_commercial",
                    Context.MODE_PRIVATE);
            editor = mPreferences.edit();

            long shownCommercialId = (long) mPreferences.getFloat(
                    "shown_commercial_id", 0);

            TrackerDAO tracker = new TrackerDAO(shownCommercialId, false,
                    false, true);
            */

            TrackerDAO tracker = new TrackerDAO(0, false,
                    false, true);

            //Log.i(LOG_TAG, "CommercialId : " + shownCommercialId);

            AppUtil.sendCommercialTracker(context, tracker);

/*
            if (!(shownCommercialId == 0)) {
                try {
                    AppUtil.sendCommercialTracker(context, tracker);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error: " + e.getLocalizedMessage());
                    Toast msg = Toast.makeText(context, "Feil: " + e,
                            Toast.LENGTH_LONG);
                    msg.show();
                }
            }
            editor.putInt("shown_commercial_id", 0);
            editor.commit();
            */
        }
    }

    // Open commercial URL if user chose to open, with keyguard
    private void openCommercialUrlWithKeyGuard(Context context) {
        mPreferences = context.getSharedPreferences("current_commercial",
                Context.MODE_PRIVATE);
        editor = mPreferences.edit();

        openCommercial = mPreferences.getBoolean("open_commercial", false);
        String commercialUrl = mPreferences
                .getString("open_commercial_url", "");

        if (openCommercial && AppUtil.isUrlValid(commercialUrl)) {
            AppUtil.openCommercialUrl(context, commercialUrl);
        }

        editor.putString("open_commercial_url", "");
        editor.putBoolean("open_commercial", false);
        editor.commit();
    }
}
