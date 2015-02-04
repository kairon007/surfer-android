package com.trysurfer.surfer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.trysurfer.surfer.user.UserLocation;

import br.com.kots.mob.complex.preferences.ComplexPreferences;

/**
 * Created by PRO on 10/31/2014.
 */
public class LocationService extends IntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public LocationService() {
        super("LocationService");
    }

    public static final String LOG_TAG = LocationService.class.getName();
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    LocationClient mLocationClient;
    Location currentLocation;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "onHandle");
        //Log.i(LOG_TAG, "Getting location");

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

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

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = mLocationClient.getLastLocation();
        mLocationClient.disconnect();

        if(currentLocation != null){
            //sendNotification("lat: " + currentLocation.getLatitude());

            ComplexPreferences complexPrefenreces = ComplexPreferences.getComplexPreferences(
                    getBaseContext(), Constants.SHAREDPREFS.APP_PREFS, MODE_PRIVATE);
            complexPrefenreces.putObject(Constants.SHAREDPREFS.USER_LOCATION, new UserLocation(currentLocation));
            complexPrefenreces.commit();
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
