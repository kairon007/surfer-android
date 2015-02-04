package com.trysurfer.surfer;

/**
 * Created by PRO on 10/9/2014.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;

import java.util.Arrays;
import java.util.List;

/*
 import com.javaone.assistant.JavaOneAppContext;
 import com.javaone.assistant.R;
 import com.javaone.assistant.SslUtil;
 import com.javaone.assistant.login.LoginActivity;
 import com.javaone.assistant.login.LoginAsyncTask;
 */

public class SplashFragment extends Fragment {

    private static final String LOG_TAG = SplashFragment.class.getName();

    private final List<String> permissions = Arrays.asList("user_location",
            "user_likes", "email", "user_birthday");
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state,
                         final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    private SharedPreferences mSharedPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.v(LOG_TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        LoginButton authButton = (LoginButton) view
                .findViewById(R.id.login_button);
        authButton.setFragment(this);
        authButton.setReadPermissions(permissions);
        authButton.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                Log.e(LOG_TAG, "Facebook login error: " + error);
                if(!AppUtil.networkAvailable(getActivity())){
                    Toast.makeText(getActivity(), "Ikke internett.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Noe gikk galt, prøv igjen. Feilmelding: " + error,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mSharedPrefs = getActivity().getSharedPreferences("app_config", Context.MODE_PRIVATE);
        boolean introShown = mSharedPrefs.getBoolean("intro_shown", false);

        if(!introShown){
            SharedPreferences.Editor mEdit = mSharedPrefs.edit();
            mEdit.putBoolean("intro_shown", true);
            mEdit.commit();

            Intent intro = new Intent(getActivity(),
                    IntroActivity.class
            );
            intro.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            getActivity().startActivity(intro);

            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            // manifest android:noHistory="true"
        }

        return view;
    }

    private void onSessionStateChange(final Session session,
                                      SessionState state, Exception exception) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.v(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        //Log.v(LOG_TAG, "onResume");

        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        //Log.v(LOG_TAG, "onPause");

        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        //Log.v(LOG_TAG, "onDestroy");

        super.onDestroy();
        uiHelper.onDestroy();
    }
}
