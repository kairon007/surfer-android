package com.trysurfer.surfer.user;

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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.UserDAO;

public class UserAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    private Context mContext = null;
    private SharedPreferences mPreferences;
    private UserDataSource datasource;

    private String serverUrl, mUserEmail, mUserFbId, mUserBirthday, mUserGender;
    private ProgressDialog dialog;
    private TextView pointCount;

    private static final String LOG_TAG = UserAsyncTask.class.getName();

    public UserAsyncTask(Context context) {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        datasource = new UserDataSource(context);
    }

    @Override
    protected void onPreExecute() {
        mPreferences = mContext.getSharedPreferences(Constants.SHAREDPREFS.APP_PREFS,
                Context.MODE_PRIVATE);
        //SurferAppContext context = SurferAppContext.getInstance();

        serverUrl = mContext.getString(R.string.base_url) + mContext.getString(R.string.user_uri);

        pointCount = (TextView) ((Activity) mContext).findViewById(R.id.point_count);

        //dialog.setMessage("Logging in, please wait.");
        //dialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(serverUrl);
        JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        String response = null;
        JSONObject json = new JSONObject();

        try {
            try {
                mUserFbId = params[0];
                mUserEmail = params[1];
                mUserBirthday = params[2];
                mUserGender = params[3];

                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                // add the user email and password to the params
                userObj.put("fb_id", mUserFbId);
                userObj.put("email", mUserEmail);
                userObj.put("birthday", mUserBirthday);
                userObj.put("gender", mUserGender);
                holder.put("user", userObj);
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
                Log.e(LOG_TAG, "" + e.getMessage());
                json.put("info", "Email and/or password are invalid. Retry!");
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
    protected void onPostExecute(JSONObject json) {
        try {
            if (json.getBoolean("success")) {
                //Log.i(LOG_TAG, "success");
                Log.i(LOG_TAG, "" + json);

                SharedPreferences.Editor editor = mPreferences.edit();

                String points = json.getJSONObject("data").getString("points");
                if(!points.equals("") || points != null){
                    pointCount.setText(points);
                }

                JSONObject jsonUserObj = json.getJSONObject("data").getJSONObject("user");

                if(!mPreferences.getBoolean(Constants.SHAREDPREFS.USER_STORED, false)) {
                    String email = "", authToken = "", fbId = "", birthday = "", gender = "";
                    long id = 0;

                    //id = Long.parseLong(jsonUserObj.getString("id"));
                    id = jsonUserObj.getLong("id");
                    fbId = jsonUserObj.getString("fb_id");
                    email = jsonUserObj.getString("email");
                    authToken = jsonUserObj.getString("auth_token");
                    birthday = jsonUserObj.getString("birthday");
                    gender = jsonUserObj.getString("gender");

                    UserDAO currentUser = new UserDAO(
                            id, fbId, email, authToken, birthday, gender
                    );

                    /*
                    try{
                        datasource.open(false);
                        datasource.createUser(currentUser);
                    } catch(Exception e){
                        e.printStackTrace();
                        Log.e(LOG_TAG, "exception: " + e);
                    } finally {
                        datasource.close();
                    }
                    */

                    editor.putLong(Constants.SHAREDPREFS.USER_ID, id);
                    editor.putString(Constants.SHAREDPREFS.USER_FB_ID, fbId);
                    editor.putString(Constants.SHAREDPREFS.USER_EMAIL, email);
                    editor.putString(Constants.SHAREDPREFS.USER_AUTH_TOKEN, authToken);
                    editor.putString(Constants.SHAREDPREFS.USER_BIRTHDAY, birthday);
                    editor.putString(Constants.SHAREDPREFS.USER_GENDER, gender);

                    editor.putBoolean(Constants.SHAREDPREFS.USER_STORED, true);
                }

                editor.putString(Constants.SHAREDPREFS.USER_POINTS, json.getJSONObject("data").getString("points"));
                editor.commit();

                //AppUtil.fetchCommercials(mContext);
            }

            //if (dialog.isShowing()) {
            //    dialog.dismiss();
            //}

            //Toast.makeText(mContext, json.getString("info"),
            //        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "" + e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        } finally {
            super.onPostExecute(json);
        }
    }
}