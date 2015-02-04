package com.trysurfer.surfer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class MainActivity extends FragmentActivity {

    private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int SETTINGS = 2;
    private static final int FRAGMENT_COUNT = SETTINGS + 1;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private MenuItem settings;
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private static final String LOG_TAG = MainActivity.class.getName();

    private LocationReceiver mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.v(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null) {
            // DO STUFF
        }

        //mAlarm = new LocationReceiver();

        printKeyHash();

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        SplashFragment splashFragment = (SplashFragment) fm
                .findFragmentById(R.id.splashFragment);
        fragments[SPLASH] = splashFragment;
        fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
        fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in
        // analytics and advertising reporting. Do so in
        // the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        // Log.v(LOG_TAG, "onPause");

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
        isResumed = false;

        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        // Log.v(LOG_TAG, "onDestroy");

        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session == null) {
            String appId = getString(R.string.app_id);
            Session session2 = new Session.Builder(getBaseContext())
                    .setApplicationId(appId).build();
            Session.setActiveSession(session2);
        } else {
            if (session != null && session.isOpened()) {
                // if the session is already open, try to show the fragment_selection fragment
                //AppUtil.startService(getApplicationContext());
                showFragment(SELECTION, false);
                //mAlarm.setAlarm(getApplicationContext());

                Log.i(LOG_TAG, "resumefragments: Logged in");
            } else {
                // otherwise present the fragment_splash screen and ask the person to login.
                AppUtil.stopService(getApplicationContext());
                showFragment(SPLASH, false);

                /*SharedPreferences mPrefs = getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
                if (mPrefs.getBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false)){
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false);
                    mEditor.commit();
                }

                mAlarm.cancelAlarm(getApplicationContext());*/

                Log.i(LOG_TAG, "resumefragments: Logged out");

            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the fragment_selection fragment is showing
        if (fragments[SELECTION].isVisible()) {
            if (menu.size() == 0) {
                settings = menu.add(R.string.settings);
            }
            return true;
        } else {
            menu.clear();
            settings = null;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(settings)) {
            showSettingsFragment();
            return true;
        }
        return false;
    }

    public void showSettingsFragment() {
        showFragment(SETTINGS, true);
    }

    private void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (session == null) {
                String appId = getString(R.string.app_id);
                Session session2 = new Session.Builder(getBaseContext())
                        .setApplicationId(appId).build();
                Session.setActiveSession(session2);
            } else {
                if (state.equals(SessionState.OPENED)/*state.isOpened()*/) {
                    // If the session state is open: show the authenticated fragment
                    showFragment(SELECTION, false);
                    //AppUtil.startService(getApplicationContext());
                    Log.i(LOG_TAG, "onsessionstatechange: Logged in");

                    //mAlarm.setAlarm(getApplicationContext());



                    System.out.println("SELECTION");
                } else if (state.isClosed()) {
                    // If the session state is closed: show the login fragment
                    showFragment(SPLASH, false);
                    AppUtil.stopService(getApplicationContext());
                    Log.i(LOG_TAG, "onsessionstatechange: Logged out");

                    /*SharedPreferences mPrefs = getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
                    if (mPrefs.getBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false)){
                        SharedPreferences.Editor mEditor = mPrefs.edit();
                        mEditor.putBoolean(Constants.SHAREDPREFS.COMMERCIAL_COOLDOWN, false);
                        mEditor.commit();
                    }*/

                    //mAlarm.cancelAlarm(getApplicationContext());
                }
            }
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.trysurfer.surfer", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("------KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }
}
