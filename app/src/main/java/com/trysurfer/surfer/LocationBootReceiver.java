package com.trysurfer.surfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by PRO on 10/31/2014.
 */
// BEGIN_INCLUDE(autostart)
public class LocationBootReceiver extends BroadcastReceiver {
    LocationReceiver alarm = new LocationReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
//END_INCLUDE(autostart)
