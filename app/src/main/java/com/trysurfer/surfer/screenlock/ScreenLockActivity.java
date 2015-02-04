package com.trysurfer.surfer.screenlock;

/**
 * Created by PRO on 10/9/2014.
 */
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.commercial.CommercialsDataSource;
import com.trysurfer.surfer.model.CommercialDAO;
import com.trysurfer.surfer.model.TrackerDAO;

import net.frakbot.glowpadbackport.GlowPadView;

public class ScreenLockActivity extends Activity /*implements OnTriggerListener */ {

    private String LOG_TAG = ScreenLockActivity.class.getName();
    //private GlowPadView mGlowPadView;
    private boolean flagsSet = false;
    private ImageView background;
    private List<CommercialDAO> commercialList = null;
    private CommercialDAO chosenCommercial;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateActivityFlags();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenlock);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
/*
        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
        // mGlowPadView = new GlowPadView(getApplicationContext());

        mGlowPadView.setOnTriggerListener(this);

        // uncomment this to make sure the activity_glowpad doesn't vibrate on touch
        mGlowPadView.setVibrateEnabled(false);

        // uncomment this to hide targets
        mGlowPadView.setShowTargetsOnIdle(true);

        // mGlowPadView.bringToFront();
    */

		/* ------ GLOWPADVIEW END ------ */

        background = (ImageView) findViewById(R.id.glow_pad_background);
        //commercialList = null;

        mContext = getApplicationContext();
        mPreferences = mContext.getSharedPreferences("current_commercial",
                Context.MODE_PRIVATE);
        //editor = mPreferences.edit();

        chosenCommercial = chooseCommercial();
        lauchCommercialImage(chosenCommercial);
        startPhoneStateListener();

        final GlowPadView glowPad = (GlowPadView) findViewById(R.id.incomingCallWidget);

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
                        finish();
                        break;

                    case R.drawable.ic_lockscreen_open:
                        //Toast.makeText(this, "Google selected", Toast.LENGTH_SHORT).show();

                        openCommercial();
                        finish();
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

        glowPad.setVibrateEnabled(false);
        glowPad.setShowTargetsOnIdle(true);

    }

    protected void closeCommercial() {
        //commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(chosenCommercial.getId(), false,
                true, false);

        AppUtil.sendCommercialTracker(mContext, tracker);

        finish();
    }

    // If user slides to the left
    protected void openCommercial() {
        //commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(chosenCommercial.getId(), true,
                false, false);

        AppUtil.sendCommercialTracker(mContext, tracker);

        if (AppUtil.isUrlValid(chosenCommercial.getUrl())) {
            KeyguardManager keyguardManager = (KeyguardManager) mContext
                    .getSystemService(Context.KEYGUARD_SERVICE);

            if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                AppUtil.openCommercialUrl(mContext,
                        chosenCommercial.getUrl());
            } else {
                editor = mPreferences.edit();
                editor.putString("open_commercial_url",
                        chosenCommercial.getUrl());
                editor.putBoolean("open_commercial", true);
                editor.commit();
            }
        }
        finish();
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

    private void lauchCommercialImage(CommercialDAO commercial) {
        try {
            new DisplayStoredImageAsyncTask(this).execute(commercial);
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(LOG_TAG, "" + e.getLocalizedMessage());
        }
    }

    protected CommercialDAO chooseCommercial() {
        commercialList = fetchCommercialsFromDb();
        if (commercialList.size() > 0 && commercialList != null) {
            int rng = new Random().nextInt(commercialList.size());
            chosenCommercial = commercialList.get(rng);

            //editor.putFloat("shown_commercial_id", chosenCommercial.getId());
            //editor.commit();
            Log.e(LOG_TAG, "chosencommercials notnull");
            return chosenCommercial;
        }
        return null;
    }

    // Fetching stored commercials
    protected List<CommercialDAO> fetchCommercialsFromDb() {
        CommercialsDataSource datasource = new CommercialsDataSource(
                getApplicationContext());
        List<CommercialDAO> commercialList = null;
        try {
            datasource.open(true);
            commercialList = datasource.getAllCommercials();

            // Log.i(LOG_TAG, "" + commercialList);
            Log.i(LOG_TAG, "list size: " + commercialList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            datasource.close();
            if (commercialList != null) {
                Log.e(LOG_TAG, "fetchcommercialsfromdb: notnull");
                return commercialList;
            }
        }
        Log.e(LOG_TAG, "fetchcommercialsfromdb: null");
        return null;
    }

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
            return true; // because I handled the event
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

    // Start phone state listener
    private void startPhoneStateListener() {
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    // StateListener.
    class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(LOG_TAG, "call Activity off hook");
                    finish();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    };
}


