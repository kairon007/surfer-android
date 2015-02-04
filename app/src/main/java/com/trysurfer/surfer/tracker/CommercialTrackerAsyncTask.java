package com.trysurfer.surfer.tracker;

/**
 * Created by PRO on 10/9/2014.
 */
import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.TrackerDAO;

public class CommercialTrackerAsyncTask extends AsyncTask<Object, Integer, JSONObject> {

    private Context mContext = null;
    private SharedPreferences mPrefs;

    private String mUserAuthToken, serverUrl;
    private long mUserId;

    private static final String LOG_TAG = CommercialTrackerAsyncTask.class.getName();

    public CommercialTrackerAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mPrefs = mContext.getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);

        mUserId = mPrefs.getLong(Constants.SHAREDPREFS.USER_ID, 0);
        mUserAuthToken = mPrefs.getString(Constants.SHAREDPREFS.USER_AUTH_TOKEN, "");

        //Log.i(LOG_TAG, "ID: " + mUserId);

        serverUrl = mContext.getString(R.string.base_url) + mContext.getString(R.string.commercial_tracker_uri);
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        //Log.v(LOG_TAG, "doInBackground..");

        TrackerDAO tracker = (TrackerDAO) params[0];

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(serverUrl);
        JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        JSONObject commercialTrackingObj = new JSONObject();
        String response = null;
        JSONObject json = new JSONObject();

        try {
            try {
                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                // add the user email and password to the params
                userObj.put("user_id", mUserId);
                userObj.put("auth_token", mUserAuthToken);
                commercialTrackingObj.put("commercial_id", tracker.getId());
                commercialTrackingObj.put("commercial_opened", tracker.isOpened());
                commercialTrackingObj.put("commercial_closed", tracker.isClosed());
                commercialTrackingObj.put("commercial_shown", tracker.isShown());
                holder.put("user", userObj);
                holder.put("commercial_tracker", commercialTrackingObj);
                StringEntity se = new StringEntity(holder.toString());
                post.setEntity(se);

                // setup the request headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-Type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
                json = new JSONObject(response);
            } catch (HttpResponseException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", "" + e);
                json.put("info", "Commercial tracker error. Retry!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return json;
    }

    @Override
    protected void onProgressUpdate(Integer... i) {
    }

    @Override
    protected void onPostExecute(JSONObject json){
        try {
            if (json.getBoolean("success")) {
                //Log.i(LOG_TAG, "success");
                Log.i(LOG_TAG, "" + json);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            Log.e(LOG_TAG, "" + e);
        } finally {
            super.onPostExecute(json);
        }
    }
}
