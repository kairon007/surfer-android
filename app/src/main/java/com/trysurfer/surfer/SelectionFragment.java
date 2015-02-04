package com.trysurfer.surfer;

/**
 * Created by PRO on 10/9/2014.
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.trysurfer.surfer.model.UserDAO;
import com.trysurfer.surfer.server.SurferAppContext;
import com.trysurfer.surfer.user.InterestsActivity;
import com.trysurfer.surfer.user.SettingsActivity;
import com.trysurfer.surfer.user.WithdrawActivity;

public class SelectionFragment extends Fragment {
    private TextView pointCount;
    private static final int REAUTH_ACTIVITY_CODE = 100;
    private SharedPreferences mSharedPrefs;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state,
                         final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private static final String LOG_TAG = SelectionFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

        Session session = Session.getActiveSession();
        if (session != null && session.getState().isOpened()) {
            Log.i("sessionToken", session.getAccessToken());
            Log.i("sessionTokenDueDate", session.getExpirationDate()
                    .toLocaleString());
        }
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume");

        super.onResume();
        uiHelper.onResume();

        // Check for an open session
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get user data
            makeMeRequest(session);
        }

        String pointHolder = mSharedPrefs.getString(Constants.SHAREDPREFS.USER_POINTS, "");
        if(!pointHolder.equals("")){
            pointCount.setText(pointHolder);
        }

        //AppUtil.checkIfTrackersStored(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_selection, container, false);
        Log.v(LOG_TAG, "onCreateView");

        // Check for an open session
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get user data
            makeMeRequest(session);
        }

        mSharedPrefs = getActivity().getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS,
                Context.MODE_PRIVATE);

        pointCount = (TextView) view.findViewById(R.id.point_count);
        String pointHolder = mSharedPrefs.getString(Constants.SHAREDPREFS.USER_POINTS, "");
        if(!pointHolder.equals("")){
            pointCount.setText(pointHolder);
        }

        Button btnSettings = (Button) view.findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settings = new Intent(getActivity(),
                        SettingsActivity.class
                );
                settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                getActivity().startActivity(settings);
            }
        });

        Button btnWithdraw = (Button) view.findViewById(R.id.btn_withdraw);
        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent withdraw = new Intent(getActivity(),
                        WithdrawActivity.class
                );
                withdraw.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                getActivity().startActivity(withdraw);
            }
        });

        /*
        Button btnLocation = (Button) view.findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent location = new Intent(getActivity(),
                        LocationActivity.class);
                getActivity().startActivity(location);
            }
        });
        */

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REAUTH_ACTIVITY_CODE) {
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "onPause");

        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");

        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(final Session session,
                                      SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            // Get the user's data.
            makeMeRequest(session);
        }
    }

    private void makeMeRequest(final Session session) {
        Log.v(LOG_TAG, "makeMeRequest");

        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                // If the response is successful
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        try {
                            syncUserInfo(user);
                            //Log.i(LOG_TAG, response.toString());

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error: " + e.getMessage());
                        } finally {
                            //AppUtil.startUserAsyncTask(getActivity(), fb_id, email);
                            AppUtil.startService(getActivity());

                            mSharedPrefs = getActivity().getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
                            boolean interestsPicked = mSharedPrefs.getBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, false);

                            if(!interestsPicked){
                                SharedPreferences.Editor mEdit = mSharedPrefs.edit();
                                mEdit.putBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, true);
                                mEdit.commit();

                                Intent interests = new Intent(
                                    getActivity(), InterestsActivity.class
                                );
                                interests.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                getActivity().startActivity(interests);

                                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                // manifest android:noHistory="true"
                            }
                        }
                    }
                }
                if (response.getError() != null) {
                    Log.e(LOG_TAG, "makeMeRequest, response: " + response.getError());
                    handleError(response.getError());
                }
            }
        });
        request.executeAsync();
    }

    private void syncUserInfo(GraphUser user) {
        String fb_id = "", email = "", gender = "", birthday = "", location = "";

        try{
            email = user.getInnerJSONObject().getString("email");
            fb_id = user.getId();
            gender = (String) user.getProperty("gender");
            birthday = user.getBirthday(); //MM/DD/YYYY
            //location = user.getLocation().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NumberFormatException nfe) {
            Log.e(LOG_TAG, "NFE error: " + nfe.getMessage());
        }  catch (Exception e){
            e.printStackTrace();
        } finally {
            Log.i(LOG_TAG,
                    "FB INFO. ID: " + fb_id
                    + ", email: " + email
                    + ", gender: " + gender
                    + ", birthday: " + birthday
                    /*+ ", location: " + location*/
            );
        }

        AppUtil.startUserAsyncTask(getActivity(), fb_id, email, birthday, gender);


        // Example: access via key for array (languages)
        // - requires user_likes permission
        /*
        JSONArray languages = (JSONArray)user.getProperty("languages");
        if (languages.length() > 0) {
            ArrayList<String> languageNames = new ArrayList<String> ();
            for (int i=0; i < languages.length(); i++) {
                JSONObject language = languages.optJSONObject(i);
                // Add the language name to a list. Use JSON
                // methods to get access to the name field.
                languageNames.add(language.optString("name"));
            }
            userInfo.append(String.format("Languages: %s\n\n",
                    languageNames.toString()));
        }
        */
    }

    // Set App context for authentication
    private void setAppContext() {
        Log.v(LOG_TAG, "setAppContext");
        SurferAppContext context = SurferAppContext.getInstance();
        context.setBaseUrl(getString(R.string.base_url));
    }

    private void handleError(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            //dialogBody = getString(R.string.error_dialog_default_text);
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // tell the user what happened by getting the message id, and
                    // retry the operation later
                    String userAction = (error.shouldNotifyUser()) ? "" :
                            getString(error.getUserActionMessageId());
                    //dialogBody = getString(R.string.error_authentication_retry, userAction);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                            //startActivity(intent);
                        }
                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                    // close the session and reopen it.
                    //dialogBody = getString(R.string.error_authentication_reopen);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };
                    break;

                case PERMISSION:
                    // request the publish permission
                    //dialogBody = getString(R.string.error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //pendingAnnounce = true;
                            //requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                case THROTTLING:
                    // this is usually temporary, don't clear the fields, and
                    // ask the user to try again
                    //dialogBody = getString(R.string.error_server);
                    break;

                case BAD_REQUEST:
                    // this is likely a coding error, ask the user to file a bug
                    //dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // an unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug
                    //dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
                    break;
            }
        }

        String title = error.getErrorUserTitle();
        String message = error.getErrorUserMessage();
        if (message == null) {
            message = dialogBody;
        }
        if (title == null) {
            //title = getResources().getString(R.string.error_dialog_title);
        }

        /*
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(title)
                .setMessage(message)
                .show();
        */
    }
}
