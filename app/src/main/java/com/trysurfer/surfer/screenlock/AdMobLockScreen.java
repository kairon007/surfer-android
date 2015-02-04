package com.trysurfer.surfer.screenlock;

/**
 * Created by PRO on 10/10/2014.
 */

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.purchase.InAppPurchase;
import com.google.android.gms.ads.purchase.InAppPurchaseListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.CommercialCooldownReceiver;
import com.trysurfer.surfer.CommercialCooldownService;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.model.UserDAO;
import com.trysurfer.surfer.user.UserDataSource;
import com.trysurfer.surfer.user.UserLocation;

import net.frakbot.glowpadbackport.GlowPadView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.kots.mob.complex.preferences.ComplexPreferences;

/**
 * Main Activity. Inflates activity_main activity xml and child fragments.
 */
public class AdMobLockScreen extends ActionBarActivity {

    private GlowPadView glowPad = null;
    private boolean flagsSet = false;

    private Context mContext = null;
    private View screenLockView = null;
    private ViewGroup screenLockViewParent = null;
    private SharedPreferences mPrefs = null;
    private TextView currentTime;

    private CommercialCooldownReceiver CdService;

    private String LOG_TAG = AdMobLockScreen.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateActivityFlags();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admob);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CdService = new CommercialCooldownReceiver();

        startPhoneStateListener();
        //initInterstitialAdMob();
        mContext = getApplicationContext();

        glowPad = (GlowPadView) findViewById(R.id.glowpadtest);
        glowPad.setVibrateEnabled(false);
        glowPad.setShowTargetsOnIdle(true);
        //glowPad.setPointsMultiplier(mults[multIndex]);
        glowPad.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            @Override
            public void onGrabbed(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onReleased(View v, int handle) {
                glowPad.ping();
            }

            @Override
            public void onTrigger(View v, int target) {
                //Toast.makeText(ScreenLockActivity.this, "Target triggered! ID=" + target, Toast.LENGTH_SHORT).show();
                //glowPad.reset(true);

                final int resId = glowPad.getResourceIdForTarget(target);
                switch (resId) {
                    case R.drawable.ic_lockscreen_unlock:
                        //Toast.makeText(this, "Camera selected", Toast.LENGTH_SHORT).show();
                        closeCommercial();
                        break;

                    case R.drawable.ic_lockscreen_open:
                        //Toast.makeText(this, "Google selected", Toast.LENGTH_SHORT).show();
                        openCommercial();
                        break;
                    default:
                    // Code should never reach here.
                }
            }

            @Override
            public void onGrabbedStateChange(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onFinishFinalAnimation() {
                // Do nothing
            }
        });

        currentTime = (TextView) findViewById(R.id.test_time);

        Thread myThread = null;

        Runnable myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                //| View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    //TextView txtCurrentTime = (TextView) findViewById(R.id.myText);
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();
                    String curTime = hours + ":" + minutes + ":" + seconds;
                    currentTime.setText(curTime);
                    //Log.i(LOG_TAG, "Time: " + curTime);
                }catch (Exception e) {}
            }
        });
    }


    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        glowPad.ping();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void closeCommercial() {
        SharedPreferences mPrefs = getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, true);
        mEditor.commit();

        //CdService.setAlarm(getApplicationContext());

        //commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(0, false,
                true, false);
        AppUtil.sendCommercialTracker(mContext, tracker);

        finish();
    }

    // If user slides to the left
    protected void openCommercial() {
        mPrefs = getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, true);
        mEditor.commit();

        //CdService.setAlarm(getApplicationContext());

        //commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(0, true,
                false, false);

        AppUtil.sendCommercialTracker(mContext, tracker);

        screenLockView = findViewById(R.id.glowpadtest);
        screenLockViewParent = (ViewGroup) screenLockView.getParent();
        if(screenLockViewParent != null){
            screenLockViewParent.removeView(screenLockView);
        }

        KeyguardManager keyguardManager = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);

        if (!keyguardManager.inKeyguardRestrictedInputMode()) {
            //AppUtil.openCommercialUrl(mContext,
            //        chosenCommercial.getUrl());
        } else {
            updateActivityFlags();
            //mEditor = mPrefs.edit();
            //mEditor.putString("open_commercial_url",
            //        chosenCommercial.getUrl());
            //mEditor.putBoolean("open_commercial", true);
            //mEditor.commit();
        }

        //finish();
    }

    public void updateActivityFlags() {
        if (!flagsSet) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            flagsSet = true;
        } else if (flagsSet) {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            flagsSet = false;
        }
    }

    // Start phone state listener
    private void startPhoneStateListener() {
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    // StateListener.
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(LOG_TAG, "call Activity state ringing");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(LOG_TAG, "call Activity off hook");
                    finish();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(LOG_TAG, "call Activity state idle");
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
        return;
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                // || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            // this is where I can do my stuff
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            // || (event.getKeyCode() == KeyEvent.KEYCODE_POWER))
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            // System.out.println("KEYCODE_HOME PRESSED");
            // return true;
            return true;
        }
        return false;
    }

    /**
     * A placeholder fragment containing a simple view. This fragment
     * would include your content.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }
    }

    /**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;
        private AdRequest adRequest;

        private String adMobContentUrl = "";

        private static String LOG_TAG = AdFragment.class.getName();

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            mAdView = (AdView) getView().findViewById(R.id.adView);
            //mAdView.setInAppPurchaseListener(this);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded(){
                    if(adRequest != null){
                        //adMobContentUrl = adRequest.getContentUrl();
                        //Log.i(LOG_TAG, adMobContentUrl);
                        SharedPreferences mPrefs = getActivity().getSharedPreferences(
                                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor mEditor = mPrefs.edit();
                        mEditor.putBoolean("is_ad_loaded", true);
                        mEditor.commit();
                    }
                }
                @Override
                public void onAdFailedToLoad(int errorCode){
                    String message = String.format("onAdFailedToLoad (%s)",
                            getErrorReason(errorCode));
                    Log.e(LOG_TAG, message);
                    getActivity().finish();
                }
                @Override
                public void onAdOpened() {
                    getActivity().finish();
                }
                @Override
                public void onAdClosed(){
                    getActivity().finish();
                    return;
                }
                @Override
                public void onAdLeftApplication(){
                    getActivity().finish();
                    return;
                }
            });


            //UserDataSource datasource;
            //datasource = new UserDataSource(getActivity());

            SharedPreferences mPreferences = getActivity().getSharedPreferences(
                    Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
            UserDAO currentUser = new UserDAO(
                    mPreferences.getLong(Constants.SHAREDPREFS.USER_ID, 0),
                    mPreferences.getString(Constants.SHAREDPREFS.USER_FB_ID, ""),
                    mPreferences.getString(Constants.SHAREDPREFS.USER_EMAIL, ""),
                    mPreferences.getString(Constants.SHAREDPREFS.USER_AUTH_TOKEN, ""),
                    mPreferences.getString(Constants.SHAREDPREFS.USER_BIRTHDAY, ""),
                    mPreferences.getString(Constants.SHAREDPREFS.USER_GENDER, "")
            );

            /*
            try{
                datasource.open(true);
                currentUser = datasource.getUser("533175537");
            } catch(Exception e){
                e.printStackTrace();
                Log.e(LOG_TAG, "exception: " + e);
            } finally {
                datasource.close();
                Log.i(LOG_TAG, "id: " + currentUser.getId()+
                "fbid: " + currentUser.getFbId() + "email " + currentUser.getEmail() +
                "gender: " + currentUser.getEmail() + "birthday: " + currentUser.getBirthday());
            }
            */

            adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("ABCDEF012345")
                    .build();


            ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(
                    getActivity(), Constants.SHAREDPREFS.APP_PREFS, MODE_PRIVATE);
            UserLocation userLocation = complexPreferences.getObject(
                    Constants.SHAREDPREFS.USER_LOCATION, UserLocation.class);

            // Sjekke om userLocation == null
            if (currentUser != null && userLocation != null) {

                int gender = 0;
                if (currentUser.getGender().equals("male")) {
                    gender = AdRequest.GENDER_MALE;
                } else if (currentUser.getGender().equals("female")) {
                    gender = adRequest.GENDER_FEMALE;
                } else if (currentUser.getGender().equals("unknown")) {
                    gender = adRequest.GENDER_UNKNOWN;
                } else {
                    gender = adRequest.GENDER_UNKNOWN;
                }

                Date birthday = null;
                try {
                    Log.i(LOG_TAG, "user birthday: " + currentUser.getBirthday());
                    birthday = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(currentUser.getBirthday());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("ABCDEF012345")
                    .setBirthday(birthday)
                    .setGender(gender)
                    .setLocation(userLocation.getUserLocation())
                    //.tagForChildDirectedTreatment(true/false) // age < 18 ?
                    .build();


                /* Lage metode for dette. Brukes dette i det hele tatt av admob??
                Builder adRequestBuilder = new AdRequest.Builder();
                String[] keywords = getResources().getStringArray(R.array.key_words);
                for (String keyword : keywords) {
                    adRequestBuilder.addKeyword(keyword);
                }
                */
            }

            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_admob_ad, container, false);
        }

        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

        /**
         * Gets a string error reason from an error code.
         */
        private String getErrorReason(int errorCode) {
            String errorReason = "";
            switch (errorCode) {
                case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                    errorReason = "Internal error";
                    break;
                case AdRequest.ERROR_CODE_INVALID_REQUEST:
                    errorReason = "Invalid request";
                    break;
                case AdRequest.ERROR_CODE_NETWORK_ERROR:
                    errorReason = "Network Error";
                    break;
                case AdRequest.ERROR_CODE_NO_FILL:
                    errorReason = "No fill";
                    break;
            }
            return errorReason;
        }
    }
}

