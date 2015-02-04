package com.trysurfer.surfer.screenlock;

/**
 * Created by pro on 04.12.2014.
 */

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.CommercialCooldownReceiver;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.model.UserDAO;
import com.trysurfer.surfer.user.UserLocation;

import net.frakbot.glowpadbackport.GlowPadView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.kots.mob.complex.preferences.ComplexPreferences;


/**
 * Main Activity. Inflates activity_main activity xml and child fragments.
 */
public class InterstitialAdMobLockScreen extends ActionBarActivity {

    private GlowPadView glowPad = null;
    private boolean flagsSet = false;

    private Context mContext = null;
    private View screenLockView = null;
    private ViewGroup screenLockViewParent = null;
    private SharedPreferences mPrefs = null;

    private InterstitialAd mInterstitialAd;

    private CommercialCooldownReceiver CdService;

    private String LOG_TAG = AdMobLockScreen.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateActivityFlags();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //View locker = mInterstitialAd;

        //CdService = new CommercialCooldownReceiver();

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

        initAd();
    }

    private void initAd() {
        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        // Defined in values/strings.xml
        mInterstitialAd.setAdUnitId("ca-app-pub-8621659814260319/9888294985");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.e(LOG_TAG, "" + getErrorReason(errorCode));
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                //displayAd();
                mInterstitialAd.show();
            }
        });
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("ABCDEF012345")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void displayAd() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(getApplicationContext(), "Ad did not load", Toast.LENGTH_SHORT).show();
            //startGame();
        }
    }

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

        private InterstitialAd mInterstitialAd;
        //private AdRequest adRequest;

        //private String adMobContentUrl = "";

        private static String LOG_TAG = AdFragment.class.getName();

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            //initAd();
            //displayAd();
        }

        private void initAd() {
            // Create the InterstitialAd and set the adUnitId.
            mInterstitialAd = new InterstitialAd(getActivity());
            // Defined in values/strings.xml
            mInterstitialAd.setAdUnitId("ca-app-pub-8621659814260319/9888294985");
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    Log.e(LOG_TAG, "" + getErrorReason(errorCode));
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    //displayAd();
                    mInterstitialAd.show();
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("ABCDEF012345")
                    .build();
            mInterstitialAd.loadAd(adRequest);
        }

        private void displayAd() {
            // Show the ad if it's ready. Otherwise toast and restart the game.
            if (mInterstitialAd != null /* && mInterstitialAd.isLoaded()*/) {
                mInterstitialAd.show();
            } else {
                Toast.makeText(getActivity(), "Ad did not load", Toast.LENGTH_SHORT).show();
                //startGame();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_interstitial, container, false);
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
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


