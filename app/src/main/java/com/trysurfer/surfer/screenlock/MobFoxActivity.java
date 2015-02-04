package com.trysurfer.surfer.screenlock;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.banner.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.CommercialCooldownReceiver;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.model.UserDAO;

import net.frakbot.glowpadbackport.GlowPadView;

/**
 * Created by pro on 12.12.2014.
 */
public class MobFoxActivity extends Activity implements AdListener {
    private RelativeLayout layout;
    private AdView mAdView;
    private AdManager mManager;

    private GlowPadView glowPad = null;
    private boolean flagsSet = false;

    private Context mContext = null;
    private View screenLockView = null;
    private ViewGroup screenLockViewParent = null;
    private SharedPreferences mPrefs = null;
    private TextView currentTime;

    private CommercialCooldownReceiver CdService;

    private String LOG_TAG = MobFoxActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateActivityFlags();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobfox);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mContext = getApplicationContext();

        initGlowPad();
        initMobFox();

        startPhoneStateListener();
        initClock();



    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        glowPad.ping();
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
    protected void onDestroy() {
        super.onDestroy();
        mManager.release();
        if(mAdView!=null)
            mAdView.release();
    }

    private void initClock() {
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

    private void initGlowPad() {
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
                        closeScreenLock();
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
    }

    private void initMobFox() {
        layout = (RelativeLayout) findViewById(R.id.adsdkContent);

        /*
        mManager = new AdManager(this,"http://my.mobfox.com/request.php",
                "d51a45b5d54b6e2b506b0de970ea6744", true);
                */
        mManager = new AdManager(this,"http://my.mobfox.com/request.php",
                "d51a45b5d54b6e2b506b0de970ea6744", true);

        mManager.setInterstitialAdsEnabled(false); //enabled by default. Allows the SDK to request static interstitial ads.
        mManager.setVideoAdsEnabled(false); //disabled by default. Allows the SDK to request video fullscreen ads.
        mManager.setPrioritizeVideoAds(false); //disabled by default. If enabled, indicates that SDK should request video ads first, and only if there is no video request a static interstitial (if they are enabled).
        mManager.setListener(this);

        if (mAdView != null) {
            removeBanner();
        }

        mAdView = new AdView(this, "http://my.mobfox.com/request.php",
                "d51a45b5d54b6e2b506b0de970ea6744", true, true);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager()
                .getDefaultDisplay().getMetrics(metrics);

        float height = metrics.heightPixels;
        float width = metrics.widthPixels;

        height = convertPixelsToDp(height, mContext);
        width = convertPixelsToDp(width, mContext);

        mAdView.setAdspaceWidth((int) width); //optional, used to set the custom size of banner placement. Without setting it, the SDK will use default sizes.
        mAdView.setAdspaceHeight((int) height);
        mAdView.setAdspaceStrict(false); //optional, tells the server to only supply banners that are exactly of desired size. Without setting it, the server could also supply smaller ads when no ad of desired size is available.



        SharedPreferences mPreferences = mContext.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        UserDAO currentUser = new UserDAO(
                mPreferences.getLong(Constants.SHAREDPREFS.USER_ID, 0),
                mPreferences.getString(Constants.SHAREDPREFS.USER_FB_ID, ""),
                mPreferences.getString(Constants.SHAREDPREFS.USER_EMAIL, ""),
                mPreferences.getString(Constants.SHAREDPREFS.USER_AUTH_TOKEN, ""),
                mPreferences.getString(Constants.SHAREDPREFS.USER_BIRTHDAY, ""),
                mPreferences.getString(Constants.SHAREDPREFS.USER_GENDER, "")
        );

        if(mPreferences.getBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, false)){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            String temp = mPreferences.getString(
                    Constants.SHAREDPREFS.USER_INTERESTS_LIST, "");
            if(!temp.equals("") && temp != null) {
                ArrayList<String> interests = gson.fromJson(temp, type);
                mAdView.setKeywords(interests);
            }

        }

        // = mPreferences.getString(Constants.SHAREDPREFS.USER_INTERESTS, "")
        // if (!(interests.equals("") && (interests != null) {
        //
        // }

        /* Lage metode for dette. Brukes dette i det hele tatt av admob??
                Builder adRequestBuilder = new AdRequest.Builder();
                String[] keywords = getResources().getStringArray(R.array.key_words);
                for (String keyword : keywords) {
                    adRequestBuilder.addKeyword(keyword);
                }
        */
        //ArrayList<String> keywords = new ArrayList<String>();
        //keywords.add("sports");
        //keywords.add("football");
        //mAdView.setKeywords(keywords); //optional, to send list of keywords (user interests) to ad server.


        Date birthday = null;
        try {
            Log.i(LOG_TAG, "user birthday: " + currentUser.getBirthday());
            birthday = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(currentUser.getBirthday());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(birthday != null){
            int age = 0;
            age = new Date().getYear() - birthday.getYear();
            Log.i(LOG_TAG, "Age: " + age);
            mAdView.setUserAge(age); //optional, sends user's age
        }

        if(!(currentUser.getGender().equals("")) && (currentUser.getGender() != null)) {
            if (currentUser.getGender().equals("male")) {
                mAdView.setUserGender(Gender.MALE);
            } else if (currentUser.getGender().equals("female")) {
                mAdView.setUserGender(Gender.FEMALE);
            } /*else if (currentUser.getGender().equals("unknown")) {
                mAdView.setUserGender(Gender.);
            } else {
                gender = adRequest.GENDER_UNKNOWN;
            }*/
        }

        //mAdView.setVisibility(View.GONE);
        mAdView.setAdListener(this);
        layout.addView(mAdView);
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
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

                    String curTime = hours + ":" + minutes;// + ":" + seconds;
                    curTime = (minutes < 10) ? curTime += "0" : curTime;
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
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onClickShowBanner(View view) {
        if (mAdView != null) {
            removeBanner();
        }
        mAdView = new AdView(this, "http://my.mobfox.com/request.php",
                "d51a45b5d54b6e2b506b0de970ea6744", true, true);

        mAdView.setAdspaceWidth(320); //optional, used to set the custom size of banner placement. Without setting it, the SDK will use default sizes.
        mAdView.setAdspaceHeight(320);
        mAdView.setAdspaceStrict(false); //optional, tells the server to only supply banners that are exactly of desired size. Without setting it, the server could also supply smaller ads when no ad of desired size is available.

        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("sports");
        keywords.add("football");
        mAdView.setKeywords(keywords); //optional, to send list of keywords (user interests) to ad server.
        mAdView.setUserAge(18); //optional, sends user's age
        mAdView.setUserGender(Gender.MALE); //optional, sends user's gender

        mAdView.setAdListener(this);
        layout.addView(mAdView);
    }

    private void removeBanner(){
        if(mAdView!=null){
            layout.removeView(mAdView);
            mAdView = null;
        }
    }

    public void onClickShowVideoInterstitial(View v) {
        mManager.requestAd();
    }

    public void adClicked() {
        //Toast.makeText(MobFoxActivity.this, "Ad clicked!", Toast.LENGTH_LONG)
        //        .show();
    }

    public void adClosed(Ad arg0, boolean arg1) {
        //Toast.makeText(MobFoxActivity.this, "Ad closed!", Toast.LENGTH_LONG)
        //        .show();
    }

    public void adLoadSucceeded(Ad arg0) {
        //Toast.makeText(MobFoxActivity.this, "Ad load succeeded!", Toast.LENGTH_LONG)
        //        .show();
        if (mManager != null && mManager.isAdLoaded())
            try{

                mManager.showAd();

                SystemClock.sleep(500);

            } catch (Exception e){

            } finally {
                mAdView.setVisibility(View.VISIBLE);
            }


        TrackerDAO tracker = new TrackerDAO(0, false,
                false, true);

        AppUtil.sendCommercialTracker(mContext, tracker);

    }

    public void adShown(Ad arg0, boolean arg1) {
        //Toast.makeText(MobFoxActivity.this, "Ad shown!", Toast.LENGTH_LONG)
        //        .show();
        Log.i(LOG_TAG, "adShown");

        /*SharedPreferences mPrefs = mContext.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putBoolean("is_ad_loaded", true);
        mEditor.commit();*/

    }

    public void noAdFound() {
        //Toast.makeText(MobFoxActivity.this, "No ad found!", Toast.LENGTH_LONG)
         //       .show();
    }

    // Close screenlock
    protected void closeScreenLock() {
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

    // If user slides to the left to open commercial
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
            screenLockViewParent.removeView(currentTime);
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
}
