package com.trysurfer.surfer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by PRO on 11/5/2014.
 */
public class CommercialCooldownService extends IntentService {

    public CommercialCooldownService(){
        super("CommercialCooldownService");
    }

    public static final String LOG_TAG = CommercialCooldownService.class.getName();
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onHandleIntent(Intent intent) {
        // cooldown false
        Log.i(LOG_TAG, "on handle");

        SharedPreferences mPreferences = getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor mEditor = mPreferences.edit();

        mEditor.putBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false);
        mEditor.commit();

        //sendNotification("Cooldown off");

        // Release the wake lock provided by the BroadcastReceiver.
        LocationReceiver.completeWakefulIntent(intent);
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg) {

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Test")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}








