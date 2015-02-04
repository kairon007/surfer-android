package com.trysurfer.surfer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by PRO on 11/5/2014.
 */
public class CommercialCooldownReceiver extends WakefulBroadcastReceiver{

    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Bundle bundle = intent.getExtras();

        Log.i("CommercialCooldown", "onReceive");
        Intent service = new Intent(context, CommercialCooldownService.class);

        startWakefulService(context, service);
    }

    public void setAlarm(Context context) {
        Log.i("CommercialCooldown", "SetAlarm");

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CommercialCooldownReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 1);

        //Long time = new GregorianCalendar().getTimeInMillis()+4*60*1000;
        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME,
                //calendar.getTimeInMillis(),
                SystemClock.elapsedRealtime()+ (4*60*1000),
                alarmIntent);

        //4*60*1000


        /*
        ComponentName receiver = new ComponentName(context, LocationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        */
    }
    // END_INCLUDE(set_alarm)


    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        /*
        ComponentName receiver = new ComponentName(context, LocationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
                */
    }


}
